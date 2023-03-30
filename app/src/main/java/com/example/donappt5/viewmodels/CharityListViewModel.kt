package com.example.donappt5.viewmodels

import android.util.Log
import android.widget.AbsListView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.R
import com.example.donappt5.adapters.CharityAdapter
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response
import com.example.donappt5.data.util.Status
import com.example.donappt5.data.util.Util
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlin.math.log

class CharityListViewModel : ViewModel() {
    var chars = MutableLiveData<Response<ArrayList<Charity>>>()
    var fillingmode = 0
    var preLast = 0
    var fillingData = false
    var lastVisible = MutableLiveData<DocumentSnapshot?>()
    var currentTag = "none"
    val repo: FirestoreService = FirestoreService

    init {
        chars.postValue(Response.loading(arrayListOf()))
        fillData()
    }


    fun fillData() {
        Log.d("fillingmode", fillingmode.toString())
        if (fillingmode == Util.FILLING_ALPHABET) {
            fillAllData()
        } else if (fillingmode == Util.FILLING_SEARCH) {
            return
        } else if (fillingmode == Util.FILLING_DISTANCE) {
//            fillDistanceData()
        } else if (fillingmode == Util.FILLING_FAVORITES) {
            fillFavoritesData()
        }
    }

    fun fillFavoritesData() {
        chars.postValue(Response.loading(null))
        repo.fillFavoritesData()
            .addOnSuccessListener { documentSnapshots: QuerySnapshot ->
                if (documentSnapshots.size() == 0) {
                    chars.postValue(Response.success(arrayListOf()))
                    return@addOnSuccessListener
                }
                val list = arrayListOf<Charity>()
                for (document in documentSnapshots) {
                    document.toCharity()?.let { list.add(it) }
                }
                chars.postValue(Response.success(list))
                fillingData = false
            }
    }


    fun fillAllData() {
        Log.d("listfrag", "filling started")
        if (fillingData) return
        fillingData = true

        if (lastVisible.value != null && chars.value?.data?.size!! >= 20) {
            chars.postValue(Response.loading(chars.value!!.data))
            repo.getCharityList(currentTag, lastVisible=lastVisible.value)
                .addOnSuccessListener(OnSuccessListener { documentSnapshots ->
                    if (documentSnapshots.size() == 0) return@OnSuccessListener
                    lastVisible?.value = documentSnapshots.documents[documentSnapshots.size() - 1]
                    val list = arrayListOf<Charity>()
                    for (document in documentSnapshots) {
                        document.toCharity()?.let { list.add(it) }
                    }
                    // TODO: По-хорошему, состояние должно быть иммутабельным, то есть каждый раз,
                    //  когда меняется состояние, оно должно пересоздаваться, на основе предыдущего.
                    //  Будем делать и стоит ли в нашем случае?
                    chars.value?.data?.plusAssign(list)
                    Log.d("charityvm", "size: ${chars.value!!.data!!.size}")
                    chars.postValue(Response.success(chars.value!!.data))
                    fillingData = false
                })
        } else {
            chars.postValue(Response.loading(null))
            repo.getCharityList(currentTag)
                .addOnSuccessListener { documentSnapshots ->
                    if (documentSnapshots.size() == 0) {
                        return@addOnSuccessListener
                    }
                    lastVisible.value = documentSnapshots.documents[documentSnapshots.size() - 1]
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
