package com.example.donappt5.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.views.adapters.FriendsAdapter
import com.example.donappt5.data.model.Friend
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.data.util.Response

class ProfileViewModel : ViewModel() {
    var SELECT_PICTURE = 12341
    var loadedUri: Uri? = null
    var photourl = MutableLiveData<Response<String>>()
    var myGlobals: MyGlobals? = null
    var friendsAdapter: FriendsAdapter? = null
    var friends: ArrayList<Friend>? = null

    init {
        friends = ArrayList()
        photourl.postValue(Response.loading(null))
        FirestoreService.getPhotoUrl().addOnCompleteListener { documentSnapshot ->
            val url = documentSnapshot.result.getString("photourl")
            photourl.postValue(Response.success(url))
        }
    }
}