package com.example.donappt5.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.R
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response
import com.example.donappt5.data.util.Util
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class OwnedCharityListVeiwModel : ViewModel() {
    var chars = MutableLiveData<Response<ArrayList<Charity>>>()
    var preLast = 0
    var fillingData = false
    var lastVisible = MutableLiveData<DocumentSnapshot?>()
    val repo: FirestoreService = FirestoreService

    init {
        chars.postValue(Response.loading(arrayListOf()))
        fillData()
    }

    fun fillData() {
        if (fillingData) return
        fillingData = true
        if (lastVisible.value != null && chars.value?.data?.size!! >= 20) {
            chars.postValue(Response.loading(chars.value!!.data))
            repo.getOwnedCharities(lastVisible.value)
                .addOnSuccessListener(OnSuccessListener { documentSnapshots ->
                    if (documentSnapshots.size() == 0) {
                        return@OnSuccessListener
                    }
                    lastVisible.postValue(documentSnapshots.documents[documentSnapshots.size() - 1])
                    val list = arrayListOf<Charity>()
                    for (document in documentSnapshots) {
                        document.toCharity()?.let { list.add(it) }
                    }
                    chars.value?.data?.plusAssign(list)
                    chars.postValue(Response.success(chars.value!!.data))
                    fillingData = false
                })
        } else {
            chars.postValue(Response.loading(null))
            repo.getOwnedCharities()
                .addOnSuccessListener { documentSnapshots ->
                    if (documentSnapshots.size() == 0) {
                        return@addOnSuccessListener
                    }
                    lastVisible.postValue(documentSnapshots.documents[documentSnapshots.size() - 1])
                    val list = arrayListOf<Charity>()
                    for (document in documentSnapshots) {
                        document.toCharity()?.let { list.add(it) }
                    }
                    chars.postValue(Response.success(list))
                    fillingData = false
                }
        }
    }

    fun getChars() : LiveData<Response<ArrayList<Charity>>> {
        return chars
    }



}