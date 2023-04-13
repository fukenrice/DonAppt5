package com.example.donappt5.viewmodels

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.data.model.MyClusterItem
import com.example.donappt5.data.services.FirestoreService
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.ClusterManager
import com.koalap.geofirestore.GeoFire
import com.koalap.geofirestore.GeoLocation
import com.koalap.geofirestore.GeoQuery
import com.koalap.geofirestore.GeoQueryEventListener


class CharitiesMapViewModel() : ViewModel() {
    private val earthCircumferenceMeters = 40075000.0
    var pixelDensityDpi: Double? = null
    var loadedchars: HashSet<String>? = null
    var mClusterManager = MutableLiveData<ClusterManager<MyClusterItem?>>()
    var geoQuery = MutableLiveData<GeoQuery>()
    var location = MutableLiveData<LatLng>()
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()
    var mFusedLocationClient = MutableLiveData<FusedLocationProviderClient>()

    init {
        latitude.value = 1000.0
        longitude.value = 1000.0
        location.value = LatLng(latitude.value!!, longitude.value!!)
        loadedchars = HashSet()
    }

    fun getPixelsPerMeter(lat: Double, zoom: Float): Double {
        if (pixelDensityDpi == null) return 0.1
        val pixelsPerTile: Double =
            256 * (pixelDensityDpi!! / 160)
        val numTiles = Math.pow(2.0, zoom.toDouble())
        val metersPerTile = Math.cos(Math.toRadians(lat)) * earthCircumferenceMeters / numTiles
        return pixelsPerTile / metersPerTile
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        mFusedLocationClient.value?.lastLocation?.addOnCompleteListener { task ->
            val location = task.result
            if (location == null) {
                requestNewLocationData()
            } else {
                Log.d(
                    "LocatorActivityTracker",
                    "getlastlocation: lat, long: " + location.latitude + location.longitude
                )
                this.location.value = LatLng(location.latitude, location.longitude)
                latitude.value = location.latitude
                longitude.value = location.longitude
                setQuery()
            }
        }
    }

    fun setQuery() {
        val ref = FirebaseFirestore.getInstance().collection("charitylocations")
        val geoFireuserlocation = GeoFire(ref)
        geoQuery.value = geoFireuserlocation.queryAtLocation(GeoLocation(latitude.value!!, longitude.value!!), 25.0)
        Log.d("geoquery", "Am I even here?2")
        geoQuery.value!!.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                println(
                    String.format(
                        "Key %s entered the search area at [%f,%f]",
                        key,
                        location.latitude,
                        location.longitude
                    )
                )
                if (!loadedchars!!.contains(key)) {
                    loadedchars!!.add(key)
                    FirestoreService.getCharityData(key)?.addOnSuccessListener {
                        if (it != null) {
                            val offsetItem =
                                MyClusterItem(location.latitude, location.longitude, it.toCharity()?.name, "snippet", it.toCharity()?.firestoreID)
                            mClusterManager.value?.addItem(offsetItem)
                        }
                    }
                }
            }

            override fun onKeyExited(key: String) {
                println(String.format("Key %s is no longer in the search area", key))
                Log.d("geoquery", "exiteddoc:$key")
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                Log.d("geoquery", "moveddoc:$key")
                if (!loadedchars!!.contains(key)) {
                    loadedchars!!.add(key)
                    val offsetItem =
                        MyClusterItem(location.latitude, location.longitude, key, "snippet", key)
                    mClusterManager.value?.addItem(offsetItem)
                }
            }

            override fun onGeoQueryReady() {
                Log.d("geoquery", "ready")
            }

            override fun onGeoQueryError(exception: Exception) {
                Log.d("geoquery", "dam:$exception")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient.value!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }


    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            location.value = LatLng(
                mLastLocation!!.latitude, mLastLocation.longitude
            )
            latitude.value = mLastLocation.latitude
            longitude.value = mLastLocation.longitude
        }
    }
}
