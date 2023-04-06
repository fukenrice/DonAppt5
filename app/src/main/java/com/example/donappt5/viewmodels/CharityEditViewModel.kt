package com.example.donappt5.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response
import com.koalap.geofirestore.GeoLocation

class CharityEditViewModel : ViewModel() {
    lateinit var charity: Charity
    val repo: FirestoreService = FirestoreService
    val imageUri = MutableLiveData<Uri?>(null)
    val edited = MutableLiveData<Response<Boolean>>()
    val tags = MutableLiveData<Response<Boolean>>()
    val deleted = MutableLiveData<Response<Boolean>>()
    val isNameFree = MutableLiveData<Response<Boolean>>()
    var SELECT_PICTURE = 2878

    init {

    }

    fun putTags(tags: Map<String, Boolean>) {
        repo.putTags(charity.firestoreID, tags)
            .addOnSuccessListener {
                this.tags.postValue(Response.success(true))
                Log.d("CharityEditVeiwModel", "putTags: success")
            }
            .addOnFailureListener {
                this.tags.postValue(Response.error(it.message.toString(), null))
            }
    }

    fun editCharity(fields: MutableMap<String, Any>) {
        if (imageUri.value != null) {
            repo.uploadCharityImage(charity.firestoreID, imageUri.value!!)
                .addOnSuccessListener {
                    repo.getImageUrl(charity.firestoreID).addOnSuccessListener { uri ->
                        fields["photourl"] = uri.toString()
                        repo.editCharity(charity.firestoreID, fields).addOnSuccessListener {
                            edited.postValue(Response.success(true))
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d("CharityEditVeiwModel", "editCharity: failed to upload image")
                }
        } else {
            repo.editCharity(charity.firestoreID, fields)
                .addOnSuccessListener {
                edited.postValue(Response.success(true))
            }
                .addOnFailureListener {
                    edited.postValue(Response.error(it.message.toString(), false))
                }
        }
    }

    fun deleteCharity() {
        repo.deleteCharity(charity.firestoreID)
            .addOnSuccessListener {
                deleted.postValue(Response.success(true))
            }
            .addOnFailureListener {
                deleted.postValue(Response.error(it.message.toString(), false))
            }
    }

    fun checkName(name: String) {
        repo.checkName(name).addOnCompleteListener() {
            if (it.isSuccessful) {
                if (it.result.size() != 0 && name != charity.name) {
                    isNameFree.postValue(Response.success(false))
                } else {
                    isNameFree.postValue(Response.success(true))
                }
            }
        }
    }

    fun checkCreationName(name: String) {
        repo.checkName(name).addOnCompleteListener() {
            if (it.isSuccessful) {
                if (it.result.size() != 0) {
                    isNameFree.postValue(Response.success(false))
                } else {
                    isNameFree.postValue(Response.success(true))
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        val resultingactivity = data.getStringExtra("resultingactivity")
        Log.d("progresstracker", "resulted activity $resultingactivity")
        if (resultingactivity != null) {
            if (resultingactivity == "LocatorActivity") {
                onLocatorActivityResult(data)
            } else if (resultingactivity == "TagsActivity") {
                onTagsActivityResult(data)
            }
        } else {
            Thread {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url from data
                        val selectedImageUri = data.data
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            imageUri.postValue(selectedImageUri)
                            Log.i("imageloader", "Image URI : $imageUri")
                        }
                    }
                }
            }.start()
        }
    }

    // TODO
    protected fun onLocatorActivityResult(data: Intent?) {
        if (data == null) {
            return
        }
        val coordsgiven = data.getBooleanExtra("locationgiven", false)
        val latitude = data.getDoubleExtra("latitude", 0.0)
        val longitude = data.getDoubleExtra("longitude", 0.0)
        if (coordsgiven) {
            FirestoreService.setCharityLocation(charity.firestoreID, GeoLocation(latitude, longitude))
        }
    }

    fun onTagsActivityResult(data: Intent) {
        val tags = mutableMapOf(
            "art" to false,
            "kids" to false,
            "poverty" to false,
            "science&research" to false,
            "healthcare" to false,
            "education" to false
        )
        tags["art"] = data.getBooleanExtra("art", false)
        tags["kids"] = data.getBooleanExtra("kids", false)
        tags["poverty"] = data.getBooleanExtra("poverty", false)
        tags["science&research"] = data.getBooleanExtra("science&research", false)
        tags["healthcare"] = data.getBooleanExtra("healthcare", false)
        tags["education"] = data.getBooleanExtra("education", false)
        putTags(tags)
    }
}