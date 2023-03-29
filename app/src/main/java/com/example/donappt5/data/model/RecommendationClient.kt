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

package com.example.donappt5.data.model

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.donappt5.data.util.ModelConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.IOException

/** Interface to load TfLite model and provide recommendations.  */
class RecommendationClient(private val context: Context, private val config: ModelConfig) {
    private val candidates: MutableMap<Int, Charity> = HashMap()
    private var tflite: Interpreter? = null
    lateinit var modelFile: File

    /** An immutable result returned by a RecommendationClient.  */
    data class Result(
        /** Predicted id.  */
        val id: String,
        /** A sortable score for how good the result is relative to others. Higher should be better.  */
        val confidence: Float
    ) {
        override fun toString(): String {
            return String.format("[%s] confidence: %.3f", id, confidence)
        }
    }

    /** Load the TF Lite model and dictionary.  */
    suspend fun load(callback: (suspend ()->Unit)?) {
        downloadRemoteModel(callback)
    }

    /** Load TF Lite model.  */
    private suspend fun loadLocalModel() {
        return withContext(Dispatchers.IO) {
            try {
                initializeInterpreter()
                Log.v(TAG, "TFLite model loaded.")
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }
    }

    private fun initializeInterpreter() {
        tflite = Interpreter(modelFile)
        if (tflite == null) {
            Log.d(TAG, "tflite still null")
        } else {
            Log.d(TAG, tflite.toString())
        }
    }

    /** Load recommendation candidate list.  */
    private suspend fun loadCandidateList() {
    }

    /** Given a list of selected items, preprocess to get tflite input.  */
    @Synchronized
    private suspend fun preprocess(userID: String): Array<Array<String>> {
        return withContext(Dispatchers.Default) {
            val inputContext = arrayOf(arrayOf(userID))
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
        Log.d(TAG, "recommend run" + tflite)
        return withContext(Dispatchers.Default) {
            val user = FirebaseAuth.getInstance().currentUser
            val inputs: Array<Array<String>> = preprocess(user?.uid ?: "0")
            Log.d(TAG, "" + inputs.size + " " + inputs.toString())
            // Run inference.
            val outputIds: Array<Array<String>> = Array(1){Array(config.outputLength){"0"} }

            val confidences = arrayOf(FloatArray(config.outputLength))
            val outputs: MutableMap<Int, Any> = HashMap()
            outputs[config.outputIdsIndex] = outputIds
            outputs[config.outputScoresIndex] = confidences
            tflite?.let {
                Log.d(TAG, "tflite not null")
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
        outputIds: Array<Array<String>>, confidences: Array<FloatArray>, userID: String
    ): List<Result> {
        return withContext(Dispatchers.Default) {
            val results = ArrayList<Result>()

            // Add recommendation results. Filter null or contained items.
            for (i in outputIds[0].indices) {
                if (results.size >= config.topK) {
                    Log.v(TAG, String.format("Selected top K: %d. Ignore the rest.", config.topK))
                    break
                }
                val id = outputIds[0][i]
                val result = Result(
                    id,
                    confidences[0][i]
                )
                results.add(result)
                Log.v(TAG, String.format("Inference output[%d]. Result: %s", i, result))
            }
            results
        }
    }

    private suspend fun downloadRemoteModel(callback: (suspend ()->Unit)?) {
        downloadModel(config.remoteModelName, callback)
    }

    private suspend fun downloadModel(modelName: String, callback: (suspend ()->Unit)?) {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel(modelName, DownloadType.LOCAL_MODEL, conditions)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(context, "Failed to get model file.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Downloaded remote model: $modelName ${it.result.localFilePath}")
                    config.modelPath = it.result.localFilePath?: ""
                    modelFile = it.result.file!!
                    initializeInterpreter()
                    GlobalScope.launch {
                        loadLocalModel()
                        loadCandidateList()
                        callback?.invoke()
                    }

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