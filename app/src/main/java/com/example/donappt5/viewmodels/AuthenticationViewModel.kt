package com.example.donappt5.viewmodels

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.model.User
import com.example.donappt5.data.model.User.Companion.toUser
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.views.onboarding.OnBoardingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import java.util.HashMap

class AuthenticationViewModel : ViewModel() {
    var user = MutableLiveData<Response<User>>()

    fun onLoginActivityResult() {
        val firestoreUser = FirebaseAuth.getInstance().currentUser ?: return
        FirestoreService.getUser(firestoreUser.uid).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("AuthenticationViewModel", "Error getting current user")
                return@addOnCompleteListener
            }
            val document = task.result
            if (document.exists()) {
                user.value = Response.success(document.toUser())
            } else {
                user.value = Response.success(User(
                    firestoreUser.displayName,
                    firestoreUser.email,
                    firestoreUser.photoUrl,
                    firestoreUser.uid
                ))
                FirestoreService.setUser(user.value!!.data!!)
            }
        }
    }
}