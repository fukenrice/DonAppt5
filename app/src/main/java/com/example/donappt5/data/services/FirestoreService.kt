package com.example.donappt5.data.services

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.data.model.SearchContext
import com.example.donappt5.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.koalap.geofirestore.GeoFire
import com.koalap.geofirestore.GeoLocation

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
        lastVisible: DocumentSnapshot?,
        searchContext: SearchContext?
    ): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        var query = db.collection("charities") as Query
        if (searchContext != null && !searchContext.isEmpty()) {
            Log.d("getCharityList", "Non null search context")
            if (searchContext.tags["kids"] == true) query = query.whereEqualTo("children", true)
            if (searchContext.tags["poverty"] == true) query = query.whereEqualTo("poverty", true)
            if (searchContext.tags["healthcare"] == true) query = query.whereEqualTo("healthcare", true)
            if (searchContext.tags["science"] == true) query = query.whereEqualTo("science&research", true)
            if (searchContext.tags["art"] == true) query = query.whereEqualTo("art", true)
            if (searchContext.tags["education"] == true) query = query.whereEqualTo("education", true)
            if (searchContext.name != "") { // TODO implement Algola
                query = query.orderBy("name").startAt(searchContext.name).endAt(searchContext.name + "\uf8ff")
            }
            Log.d(TAG, "getCharityList: context = ${searchContext.tags}")
            return query.get()
        }
        return if (lastVisible != null) {
            query
                .startAfter(lastVisible)
                .limit(20)
                .get()
        } else {
            query
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

        return if (lastVisible != null) {
            query
                .startAfter(lastVisible)
                .limit(20)
                .get()
        } else {
            query
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

    fun getUser(userId: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("users").document(userId).get()
    }

    fun setCharityLocation(charityId: String, location: GeoLocation) {
        val collectionReference = FirebaseFirestore.getInstance().collection("charitylocations")
        val creatingLocation: Map<String, Any> = java.util.HashMap()
        collectionReference.document(charityId).set(creatingLocation)

        val geoFirestore = GeoFire(collectionReference)
        geoFirestore.setLocation(
            charityId,
            location
        )
    }

    fun setUser(user: User) {
        val usermap: MutableMap<String, Any> =
            java.util.HashMap()
        if (user.username != null) {
            usermap["name"] = user.username!!
        }
        if (user.email != null) {
            usermap["mail"] = user.email!!
        }
        if (user.photoUrl != null) {
            usermap["photo"] = user.photoUrl.toString()
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid)
            .set(usermap)
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

    fun checkName(name: String, currentName: String? = null): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("charities").whereEqualTo("name", name).get()
    }
}