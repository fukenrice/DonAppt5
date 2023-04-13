package com.example.donappt5.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.donappt5.R
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.data.model.MyClusterItem
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.util.MyClusterRenderer
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.viewmodels.CharitiesMapViewModel
import com.example.donappt5.views.charitydescription.CharityActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.maps.android.clustering.ClusterManager
import com.koalap.geofirestore.GeoLocation

class CharitiesMapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var mapView: MapView
    private var gmap: GoogleMap? = null
    var PERMISSION_ID = 3575
    private var mLocation: Marker? = null
    lateinit var context: Context
    var myGlobals: MyGlobals? = null
    var bottomNavigationView: BottomNavigationView? = null
    private lateinit var viewModel: CharitiesMapViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charitiesmap)
        viewModel = ViewModelProvider(this)[CharitiesMapViewModel::class.java]
        context = this
        viewModel.pixelDensityDpi = context.resources.displayMetrics.densityDpi.toDouble()
        viewModel.mFusedLocationClient.value = LocationServices.getFusedLocationProviderClient(this)

        viewModel.location.observe(this) {
            if (gmap != null) {
                gmap!!.moveCamera(CameraUpdateFactory.newLatLng(viewModel.location.value!!))
                mLocation!!.position = viewModel.location.value!!
            }
        }
        if (gmap != null) {
            gmap!!.moveCamera(CameraUpdateFactory.newLatLng(viewModel.location.value!!))
            mLocation!!.position = viewModel.location.value!!
        }
        getLastLocation()
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        myGlobals = MyGlobals(context)
        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        myGlobals!!.setupBottomNavigation(context, this, bottomNavigationView!!)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID //just a code
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted. Start getting the location information
                getLastLocation()
            }
        }
    }

    private val isLocationEnabled: Boolean
        get() {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

    @SuppressLint("MissingPermission") // checked in checkPermissions()
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled) {
                viewModel.getLastLocation()
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    var clickedCharity: Charity? = null
    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.location.value!!, 10f))
        mLocation = googleMap.addMarker(MarkerOptions().position(viewModel.location.value!!).title("Marker"))
        if (mLocation == null) return
        mLocation!!.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        mLocation!!.zIndex = 1000f
        gmap = googleMap
        gmap!!.uiSettings.isZoomControlsEnabled = true
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style
            )
        )
        gmap!!.setOnCameraMoveListener {
            val latLng = gmap!!.cameraPosition.target
            val metersPerPx: Double =
                1 / viewModel.getPixelsPerMeter(latLng.latitude, gmap!!.cameraPosition.zoom)
            val wtflayout = findViewById<LinearLayout>(R.id.wtflayout)
            val width = wtflayout.width.toDouble()
            val height = wtflayout.height.toDouble()
            viewModel.geoQuery.value?.setLocation(
                GeoLocation(latLng.latitude, latLng.longitude),
                Math.sqrt(width * width + height * height) * metersPerPx / 1000
            )
        }
        viewModel.mClusterManager.value = ClusterManager(this, gmap)
        viewModel.mClusterManager.value!!.setRenderer(
            MyClusterRenderer(
                this, gmap,
                viewModel.mClusterManager.value
            )
        )
        gmap!!.setOnCameraIdleListener(viewModel.mClusterManager.value)
        gmap!!.setOnMarkerClickListener(viewModel.mClusterManager.value)

        viewModel.mClusterManager.value!!.setOnClusterItemInfoWindowClickListener { item: MyClusterItem? ->
            FirestoreService.getCharityData(item!!.id)?.addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                if (task.isSuccessful) {
                    val document = task.result ?: return@addOnCompleteListener
                    if (document.exists()) {
                        clickedCharity = document.toCharity()
                        val intent = Intent(context, CharityActivity::class.java)
                        intent.putExtra("firestoreID", clickedCharity!!.firestoreID)
                        intent.putExtra("chname", clickedCharity!!.name)
                        intent.putExtra("bdesc", clickedCharity!!.briefDescription)
                        intent.putExtra("fdesc", clickedCharity!!.fullDescription)
                        intent.putExtra("url", clickedCharity!!.photourl)
                        intent.putExtra("qiwiPaymentUrl", clickedCharity!!.paymentUrl)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getLastLocation()
        mapView.onResume()
        myGlobals!!.setSelectedItem(this, bottomNavigationView!!)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    }
}