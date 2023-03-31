package com.example.donappt5.viewmodels

import android.content.DialogInterface
import android.net.Uri
import android.provider.MediaStore
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.adapters.FriendsAdapter
import com.example.donappt5.data.model.Friend
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.MyGlobals
import com.example.donappt5.data.util.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

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