package com.example.donappt5.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.model.UserPreferences
import com.example.donappt5.data.model.UserPreferences.Companion.toUserPreferences
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OnBoardingViewModel : ViewModel() {
    val userPreferences = MutableLiveData<Response<UserPreferences>>()
    val postedPreferences = MutableLiveData<Response<String>>()
    private val repo = FirestoreService

    init {
        userPreferences.postValue(Response.loading(null))
        fetchUserPreferences()
    }

    private fun fetchUserPreferences() {
        userPreferences.postValue(Response.loading(null))
        repo.getPreferences()
            .addOnSuccessListener {
                userPreferences.postValue(Response.success(it.toUserPreferences()))
            }
            .addOnFailureListener { exception ->
                userPreferences.postValue(Response.error(exception.message.toString(), null))
            }
    }

    fun setUserPreferences(prefs: Map<String, Any>) {
        postedPreferences.postValue(Response.loading(null))
        repo.setPreferences(prefs)
            .addOnSuccessListener {
                postedPreferences.postValue(Response.success("Preferences have been successfully edited"))
            }
            .addOnFailureListener { exception ->
                postedPreferences.postValue(
                    Response.error(
                        exception.message.toString(),
                        "Something went wrong while editing preferences"
                    )
                )
            }
    }

    fun getUserPreferences(): LiveData<Response<UserPreferences>> {
        return userPreferences
    }

    fun getPostedPrefetences(): LiveData<Response<String>> {
        return postedPreferences
    }

}