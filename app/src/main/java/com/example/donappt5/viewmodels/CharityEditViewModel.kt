package com.example.donappt5.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response

class CharityEditViewModel : ViewModel() {
    lateinit var charity: Charity
    val repo: FirestoreService = FirestoreService
    val imageUri = MutableLiveData<Uri>(null)
    val edited = MutableLiveData<Response<Boolean>>()
    val tags = MutableLiveData<Response<Boolean>>()
    val deleted = MutableLiveData<Response<Boolean>>()
    val isNameFree = MutableLiveData<Response<Boolean>>()


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
}