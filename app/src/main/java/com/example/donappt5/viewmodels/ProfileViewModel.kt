package com.example.donappt5.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.data.util.Response

class ProfileViewModel : ViewModel() {
    var loadedUri: Uri? = null
    var photourl = MutableLiveData<Response<String>>()
    var myGlobals: MyGlobals? = null

    init {
        photourl.postValue(Response.loading(null))
        FirestoreService.getUserData().addOnCompleteListener { documentSnapshot ->
            val url = documentSnapshot.result.getString("photourl")
            photourl.postValue(Response.success(url))
        }
    }
}