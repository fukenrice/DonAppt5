/*
 * Copyright 2020 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 *
 */

package com.example.donappt5.helpclasses.recommendations

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.donappt5.helpclasses.Charity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer

/** Interface to load TfLite model and provide recommendations.  */
class RecommendationClient(private val context: Context, private val config: ModelConfig) {
    private val candidates: MutableMap<Int, Charity> = HashMap()
    private var tflite: Interpreter? = null

    /** An immutable result returned by a RecommendationClient.  */
    data class Result(
        /** Predicted id.  */
        val id: Int,
        /** Recommended item.  */
        val item: Charity,
        /** A sortable score for how good the result is relative to others. Higher should be better.  */
        val confidence: Float
    ) {
        override fun toString(): String {
            return String.format("[%d] confidence: %.3f, item: %s", id, confidence, item)
        }
    }

    /** Load the TF Lite model and dictionary.  */
    suspend fun load() {
        downloadRemoteModel()
        loadLocalModel()
        loadCandidateList()
    }

    /** Load TF Lite model.  */
    private suspend fun loadLocalModel() {
        return withContext(Dispatchers.IO) {
            try {
                val buffer: ByteBuffer = FileUtils.loadModelFile(
                    context.assets, config.modelPath
                )
                initializeInterpreter(buffer)
                Log.v(TAG, "TFLite model loaded.")
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }
    }

    private suspend fun initializeInterpreter(model: Any) {
        return withContext(Dispatchers.IO) {
            tflite?.apply {
                close()
            }
            if (model is ByteBuffer) {
                tflite = Interpreter(model)
            } else {
                tflite = (model as CustomModel).file?.let { Interpreter(it) }
            }
            Log.v(TAG, "TFLite model loaded.")
        }
    }

    /** Load recommendation candidate list.  */
    private suspend fun loadCandidateList() {
    }

    /** Given a list of selected items, preprocess to get tflite input.  */
    @Synchronized
    private suspend fun preprocess(userID: String): Array<String> {
        return withContext(Dispatchers.Default) {
            val inputContext = arrayOf(userID)
            inputContext
        }
    }



    /** Free up resources as the client is no longer needed.  */
    fun unload() {
        tflite?.close()
        candidates.clear()
    }

    /** Given a list of selected items, and returns the recommendation results.  */
    @Synchronized
    suspend fun recommend(): List<Result> {
        return withContext(Dispatchers.Default) {
            val user = FirebaseAuth.getInstance().currentUser
            val inputs = arrayOf<Any>(preprocess(user?.uid ?: "0"))

            // Run inference.
            val outputIds = IntArray(config.outputLength)
            val confidences = FloatArray(config.outputLength)
            val outputs: MutableMap<Int, Any> = HashMap()
            outputs[config.outputIdsIndex] = outputIds
            outputs[config.outputScoresIndex] = confidences
            tflite?.let {
                it.runForMultipleInputsOutputs(inputs, outputs)
                postprocess(outputIds, confidences, user?.uid ?: "0")
            } ?: run {
                Log.e(TAG, "No tflite interpreter loaded")
                emptyList()
            }
        }
    }

    /** Postprocess to gets results from tflite inference.  */
    @Synchronized
    private suspend fun postprocess(
        outputIds: IntArray, confidences: FloatArray, userID: String
    ): List<Result> {
        return withContext(Dispatchers.Default) {
            val results = ArrayList<Result>()

            // Add recommendation results. Filter null or contained items.
            for (i in outputIds.indices) {
                if (results.size >= config.topK) {
                    Log.v(TAG, String.format("Selected top K: %d. Ignore the rest.", config.topK))
                    break
                }
                val id = outputIds[i]
                val item = candidates[id]
                if (item == null) {
                    Log.v(TAG, String.format("Inference output[%d]. Id: %s is null", i, id))
                    continue
                }
                val result = Result(
                    id, item,
                    confidences[i]
                )
                results.add(result)
                Log.v(TAG, String.format("Inference output[%d]. Result: %s", i, result))
            }
            results
        }
    }

    private fun downloadRemoteModel() {
        downloadModel(config.remoteModelName)
    }

    private fun downloadModel(modelName: String) {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel(modelName, DownloadType.LOCAL_MODEL, conditions)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(context, "Failed to get model file.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Downloaded remote model: $modelName", Toast.LENGTH_SHORT).show()
                    GlobalScope.launch { initializeInterpreter(it.result) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Model download failed for recommendations, please check your connection.", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val TAG = "RecommendationClient"
    }
}