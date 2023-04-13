package com.example.donappt5.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.views.charitycreation.popups.ActivityConfirm
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.koalap.geofirestore.GeoFire
import com.koalap.geofirestore.GeoLocation

class ProgramEntryViewModel : ViewModel() {
    var userHasLocationsOfInterest = MutableLiveData<Boolean>()

    init {
        testUserHavingLocationsOfInterest()
        getDeviceToken()
    }

    fun testUserHavingLocationsOfInterest() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user!!.uid).collection("locations")
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    if (task.result.size() == 0) {
                        userHasLocationsOfInterest.value = false
                    }
                } else {
                    userHasLocationsOfInterest.value = false
                }
            }
    }

    fun getDeviceToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    Log.w(
                        "gettingdevicetoken",
                        "getInstanceId failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result
                Log.d("gettingdevicetokem", token!!)
                val db = FirebaseFirestore.getInstance()
                val deviceToken: MutableMap<String, Any?> =
                    HashMap()
                deviceToken["device_token"] = token
                val user = FirebaseAuth.getInstance().currentUser
                db.collection("users").document(user!!.uid).update(deviceToken)
            }
    }

    fun onLocatorActivityResult(data: Intent?) {
        if (data == null) {
            return
        }
        val coordsgiven = data.getBooleanExtra("locationgiven", false)
        val latitude = data.getDoubleExtra("latitude", -1000.0)
        val longitude = data.getDoubleExtra("longitude", -1000.0)
        if (coordsgiven && latitude > -900) {
            val user = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            val location: MutableMap<String, Any> = HashMap()
            location["latitude"] = latitude
            location["longitude"] = longitude
            db.collection("users").document(user!!.uid).collection("locations")
                .document("FirstLocation").set(location)
                .addOnSuccessListener { aVoid: Void? ->
                    val colref =
                        FirebaseFirestore.getInstance().collection("users").document(
                            user.uid
                        ).collection("locations")
                    val geoFirestore =
                        GeoFire(colref)
                    geoFirestore.setLocation(
                        "FirstLocation",
                        GeoLocation(latitude, longitude)
                    )
                }
            db.collection("userlocations").document(user.uid).set(location)
                .addOnSuccessListener { aVoid: Void? ->
                    val colref =
                        FirebaseFirestore.getInstance().collection("userlocations")
                    val geoFirestore =
                        GeoFire(colref)
                    geoFirestore.setLocation(
                        user.uid,
                        GeoLocation(latitude, longitude)
                    )
                }
            db.collection("users").document(user.uid).update(location)
        }
    }
}