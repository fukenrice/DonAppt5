package com.example.donappt5.data.services

import android.util.Log
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

object FirestoreService {
    private const val TAG = "FirestoreService"

    fun getCharityData(firestoreID: String): Task<DocumentSnapshot?>? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("charities")
                .document(firestoreID).get()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting charity details", e)
            null
        }
    }

    fun getCharityList(
        currentTag: String,
        lastVisible: DocumentSnapshot? = null
    ): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        val taggedquery = if (currentTag !== "none") {
            db.collection("charities").whereEqualTo(currentTag, true)
        } else {
            db.collection("charities")
        }
        if (lastVisible != null) {
            return taggedquery
                .startAfter(lastVisible!!)
                .limit(20)
                .get()
        } else {
            return taggedquery
                .limit(20)
                .get()
        }
    }

    fun fillFavoritesData(): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        return db.collection("users").document(user!!.uid).collection("favorites")
            .get()
    }

    fun getPreferences(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        return db.collection("users").document(user!!.uid).get()
    }

    fun setPreferences(preferences: Map<String, Any>): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        return db.collection("users").document(user!!.uid).update(preferences)
    }

    fun getOwnedCharities(lastVisible: DocumentSnapshot? = null): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val query = db.collection("charities").whereEqualTo("creatorid", user!!.uid)

        if (lastVisible != null) {
            return query
                .startAfter(lastVisible!!)
                .limit(20)
                .get()
        } else {
            return query
                .limit(20)
                .get()
        }
    }

    fun loadFav(charId: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        return db.collection("users").document(user!!.uid).collection("favorites").document(charId)
            .get()
    }

    fun removeFav(charId: String): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        return db.collection("users").document(user!!.uid).collection("favorites").document(charId)
            .delete()
    }

    fun addFav(charId: String, data: HashMap<String, Any>): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        return db.collection("users").document(user!!.uid).collection("favorites").document(
            charId
        ).set(data)
    }

}