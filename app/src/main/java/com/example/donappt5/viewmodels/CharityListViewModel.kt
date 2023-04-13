package com.example.donappt5.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.data.model.SearchContext
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Response
import com.example.donappt5.util.Util
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class CharityListViewModel() : ViewModel() {
    var chars = MutableLiveData<Response<ArrayList<Charity>>>()
    var preLast = 0
    var fillingData = false
    var lastVisible = MutableLiveData<DocumentSnapshot?>()
    val repo: FirestoreService = FirestoreService
    var searchContext = MutableLiveData<SearchContext>()
    var fillingmode = 0


    fun fillData() {
        Log.d("fillingmode", fillingmode.toString())
        if (fillingmode == Util.FILLING_ALPHABET) {
            fillPaginatedData()
        } else if (fillingmode == Util.FILLING_FAVORITES) {
            fillFavoritesData()
        }
    }

    fun fillFavoritesData() {
        Log.d("fillingmode", "filling favourites data")
        chars.postValue(Response.loading(null))
        repo.fillFavoritesData()
            .addOnSuccessListener { documentSnapshots: QuerySnapshot ->
                Log.d("favourites", "size: " + documentSnapshots.size())
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
            }.addOnFailureListener {
                chars.postValue(Response.success(arrayListOf()))
            }
    }

    fun fillSearchData() {
        chars.postValue(Response.loading(null))
        repo.getCharityList(lastVisible = lastVisible.value, searchContext.value)
            .addOnSuccessListener { documentSnapshots ->
                Log.d("charityvm", "search: " + documentSnapshots.size())
                val list = arrayListOf<Charity>()
                for (document in documentSnapshots) {
                    document.toCharity()?.let { list.add(it) }
                }

                chars.postValue(Response.success(list))
            }
            .addOnFailureListener {
                chars.postValue(Response.success(arrayListOf()))
            }
    }

    fun fillPaginatedData() {
        Log.d("listfrag", "filling started")

        if (lastVisible.value != null && chars.value?.data?.size!! >= 20) {
            chars.postValue(Response.loading(chars.value!!.data))
            repo.getCharityList(lastVisible = lastVisible.value, searchContext.value)
                .addOnSuccessListener(OnSuccessListener { documentSnapshots ->

                    if (documentSnapshots.size() == 0) return@OnSuccessListener
                    lastVisible.value = documentSnapshots.documents[documentSnapshots.size() - 1]
                    val list = arrayListOf<Charity>()
                    for (document in documentSnapshots) {
                        document.toCharity()?.let { list.add(it) }
                    }

                    // TODO: По-хорошему, состояние должно быть иммутабельным, то есть каждый раз,
                    //  когда меняется состояние, оно должно пересоздаваться, на основе предыдущего.
                    //  Будем делать и стоит ли в нашем случае?
                    chars.value?.data?.plusAssign(list)
                    chars.postValue(Response.success(chars.value!!.data))
                    fillingData = false
                })
        } else {
            chars.postValue(Response.loading(null))
            repo.getCharityList(null, searchContext.value)
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

    fun parseSearchInfo(
        kids: Boolean, poverty: Boolean, healthcare: Boolean, science: Boolean,
        art: Boolean, education: Boolean, searchName: String
    ) {
        searchContext.value = SearchContext(
            hashMapOf(
                "kids" to kids, "poverty" to poverty, "healthcare" to healthcare,
                "science" to science, "art" to art, "education" to education
            ), searchName
        )
        Log.d("search info", kids.toString() + " " + poverty + " " + healthcare + " " + science
                + " " + art + " " + education + " " + searchName)

        fillSearchData()
    }
}
