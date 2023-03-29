package com.example.donappt5.data.services

import android.util.Log
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreService {
    private const val TAG = "FirestoreService"
    suspend fun getCharityData(firestoreID: String): Charity? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("charities")
                .document(firestoreID).get().await().toCharity()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting charity details", e)
            null
        }
    }
}