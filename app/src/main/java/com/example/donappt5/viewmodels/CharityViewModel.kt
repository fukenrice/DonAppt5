package com.example.donappt5.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.services.Analytics
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response

class CharityViewModel : ViewModel() {
    private val charity = MutableLiveData<Response<Charity>>()
    private val isFavourite = MutableLiveData<Response<Boolean>>()
    private val repo: FirestoreService = FirestoreService

    init {
        charity.postValue(Response.loading(null))
        isFavourite.postValue(Response.loading(false))
    }

    fun loadFav(charId: String) {
        repo.loadFav(charId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    isFavourite.postValue(Response.success(true))
                } else {
                    isFavourite.postValue(Response.success(false))
                }
            } else {
                isFavourite.postValue(Response.error(task.exception.toString(), null))
            }
        }
    }

    fun changeFav() {
        isFavourite.postValue(Response.loading(null))
        if (isFavourite.value?.data!!) {
            repo.removeFav(charity.value?.data!!.firestoreID).addOnSuccessListener {
                isFavourite.postValue(Response.success(false))
            }
        } else {
            val namemap = HashMap<String, Any>()
            namemap["name"] = charity.value?.data!!.name
            namemap["description"] = charity.value?.data!!.fullDescription
            namemap["photourl"] = charity.value?.data!!.photourl
            repo.addFav(charity.value?.data!!.firestoreID, namemap).addOnSuccessListener {
                isFavourite.postValue(Response.success(true))
            }
        }
    }

    fun setCharity(char: Charity) {
        charity.postValue(Response.success(char))
    }

    fun logAnalytics() {
        Analytics.logAnalyticsEvent(charity.value?.data!!.firestoreID)
    }

    fun getCharity(): LiveData<Response<Charity>> {
        return charity
    }

    fun isFavourite(): LiveData<Response<Boolean>> {
        return isFavourite
    }
}
