package com.example.donappt5.viewmodels

import android.content.Intent
import android.util.Log
import android.widget.AbsListView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.donappt5.R
import com.example.donappt5.data.adapters.CharityAdapter
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.data.util.Util
import com.example.donappt5.databinding.FragmentCharityListBinding
import com.example.donappt5.views.charitydescription.CharityActivity
import com.example.donappt5.views.charitylist.CharityListActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class CharityListViewModel : ViewModel() {
    var chars = MutableLiveData<ArrayList<Charity>>()
    var adapter = MutableLiveData<CharityAdapter>()
    var fillingmode = 0
    var preLast = 0
    var fillingData = false
    var lastVisible = MutableLiveData<DocumentSnapshot?>()
    var currentTag = "none"

    init {
        chars.value = ArrayList()
    }

    fun onMyScroll(
        lw: AbsListView, firstVisibleItem: Int,
        visibleItemCount: Int, totalItemCount: Int
    ) {
        if (fillingmode == Util.FILLING_FAVORITES) return
        when (lw.id) {
            R.id.lvMain -> {
                val lastItem = firstVisibleItem + visibleItemCount
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) {
                        //to avoid multiple calls for last item
                        Log.d("Last", "Last")
                        preLast = lastItem
                        fillData()
//                        Log.d("georad", fdistance.toString())
                    }
                }
            }
        }
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
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        db.collection("users").document(user!!.uid).collection("favorites")
            .get()
            .addOnSuccessListener { documentSnapshots: QuerySnapshot ->
                if (documentSnapshots.size() == 0) return@addOnSuccessListener
                for (document in documentSnapshots) {
                    adapter.value?.objects?.add(document.toCharity())
                    adapter.value?.notifyDataSetChanged()
                }
                fillingData = false
            } //*/
    }


    fun fillAllData() {
        Log.d("listfrag", "filling started")
        if (fillingData) return
        fillingData = true
        val db = FirebaseFirestore.getInstance()
        var taggedquery: Query
        taggedquery = if (currentTag !== "none") {
            db.collection("charities").whereEqualTo(currentTag, true)
        } else {
            db.collection("charities")
        }

        if (lastVisible.value != null && adapter.value?.objects?.size!! >= 20) {
            taggedquery
                .startAfter(lastVisible!!)
                .limit(20)
                .get()
                .addOnSuccessListener(OnSuccessListener { documentSnapshots ->
                    if (documentSnapshots.size() == 0) return@OnSuccessListener
                    lastVisible?.value = documentSnapshots.documents[documentSnapshots.size() - 1]
                    for (document in documentSnapshots) {
                        adapter.value?.objects?.add(document.toCharity())
                        adapter.value?.notifyDataSetChanged()
                    }
                    fillingData = false
                }) //*/
        } else {
            taggedquery
                .limit(20)
                .get()
                .addOnSuccessListener { documentSnapshots ->
                    if (documentSnapshots.size() == 0) {
                        return@addOnSuccessListener
                    }
                    lastVisible.value = documentSnapshots.documents[documentSnapshots.size() - 1]
                    for (document in documentSnapshots) {
                        adapter.value?.objects?.add(document.toCharity())
                        adapter.value?.notifyDataSetChanged()
                    }
                    fillingData = false
                }
        }
        adapter.value?.notifyDataSetChanged()
        Log.d("listfrag", adapter.value?.objects?.size.toString())
    }

}