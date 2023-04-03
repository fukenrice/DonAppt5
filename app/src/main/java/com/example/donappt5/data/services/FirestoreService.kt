package com.example.donappt5.data.services

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

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

    fun getUserData(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        return db.collection("users").document(user!!.uid).get()
    }

    fun uploadImage(loadedUri: Uri?) {
        if (loadedUri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val file = loadedUri //Uri.fromFile(new File(pathtoimage));
            val user = FirebaseAuth.getInstance().currentUser
            val imgsref = storageRef.child("users/" + user!!.uid + "/photo")
            val uploadTask = imgsref.putFile(file)
            uploadTask.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                val db = FirebaseFirestore.getInstance()
                storageRef.child("users/" + user.uid + "/photo").downloadUrl
                    .addOnSuccessListener { uri: Uri ->
                        // Got the download URL for 'users/me/profile.png'
                        Log.d("urlgetter", uri.toString())
                        val fileUrl = uri.toString()
                        val hmap: MutableMap<String, Any> =
                            HashMap()
                        hmap["photourl"] = fileUrl!!
                        Log.d("puttingphoto", "url: $fileUrl")
                        db.collection("users")
                            .document(user.uid)
                            .update(hmap)
                    }
                    .addOnFailureListener { exception: Exception ->
                        Log.d(
                            "puttingphoto",
                            exception.toString()
                        )
                    }
            }
        } else {
            Log.d("puttingphoto", "nullpath")
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

    fun putGeoQuery() {

    }

    fun putTags(id: String, tags: Map<String, Boolean>): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("charities").document(id).update(tags)
    }

    fun uploadCharityImage(charityId: String, uri: Uri): UploadTask {
        val db = FirebaseStorage.getInstance()
        val storageRef = db.reference
        val file = uri
        val imgsref = storageRef.child("charities$charityId/photo")
        return imgsref.putFile(file)
    }

    fun getImageUrl(charityId: String): Task<Uri> {
        val db = FirebaseStorage.getInstance()
        val storageRef = db.reference
        return storageRef.child("charities$charityId/photo").downloadUrl
    }

    fun editCharity(id: String, fields: Map<String, Any>): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("charities").document(id).update(fields)
    }

    fun deleteCharity(id: String): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("charities").document(id).delete()
    }

    fun createCharity(id: String, fields: Map<String, Any>) {

    }

    fun checkName(name: String, currentName: String? = null): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("charities").whereEqualTo("name", name).get()
    }



}