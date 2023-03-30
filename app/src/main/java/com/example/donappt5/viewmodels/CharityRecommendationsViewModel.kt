package com.example.donappt5.viewmodels

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.donappt5.data.adapters.CharityAdapter
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.RecommendationClient
import com.example.donappt5.data.util.ModelConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CharityRecommendationsViewModel : ViewModel() {
    var client = MutableLiveData<RecommendationClient>()
    var adapter = MutableLiveData<CharityAdapter>()
    var config = MutableLiveData<ModelConfig>()

    suspend fun fillData() {
        client.value?.load {
            val db = FirebaseFirestore.getInstance()
            var results = client.value?.recommend()
            results?.forEach {
                db.collection("charities").document(it.id).get().addOnSuccessListener { doc ->
                    adapter.value?.objects?.add(
                        Charity(
                            doc
                        )
                    )
                    adapter.value?.notifyDataSetChanged()
                }
            }
        }
    }
}