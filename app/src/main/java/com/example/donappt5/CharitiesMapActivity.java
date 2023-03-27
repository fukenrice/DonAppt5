package com.example.donappt5;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.donappt5.helpclasses.Charity;
import com.example.donappt5.helpclasses.MyClusterItem;
import com.example.donappt5.helpclasses.MyClusterRenderer;
import com.example.donappt5.helpclasses.MyGlobals;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.clustering.ClusterManager;
import com.koalap.geofirestore.GeoFire;
import com.koalap.geofirestore.GeoLocation;
import com.koalap.geofirestore.GeoQuery;
import com.koalap.geofirestore.GeoQueryEventListener;

import java.util.HashSet;

public class CharitiesMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    MapView mapView;
    private GoogleMap gmap;
    int PERMISSION_ID = 3575;
    FusedLocationProviderClient mFusedLocationClient;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private Marker mLocation;
    double latitude = -1000;
    double longitude = -1000;
    GeoQuery geoQuery;
    private ClusterManager<MyClusterItem> mClusterManager;
    Context context;
    HashSet<String> loadedchars;
    MyGlobals myGlobals;
    BottomNavigationView bottomNavigationView;

    public void onCreate(Bundle savedInstanceState) {
        loadedchars = new HashSet<String>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charitiesmap);
        Intent intent = getIntent();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        String extra;

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.map_view);

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        context = this;


        myGlobals = new MyGlobals(context);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        myGlobals.setupBottomNavigation(context, this, bottomNavigationView);
    }

    void setQuery() {
        CollectionReference ref = FirebaseFirestore.getInstance().collection("charitylocations");
        GeoFire geoFireuserlocation = new GeoFire(ref);
        geoQuery = geoFireuserlocation.queryAtLocation(new GeoLocation(latitude, longitude), 25);

        Log.d("geoquery", "Am I even here?2");
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                Log.d("geoquery", "entereddoc:" + key);
                //Marker newmarker = gmap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(key));;
                //loadedmarkers.add(newmarker);
                if (!loadedchars.contains(key)) {
                    loadedchars.add(key);
                    MyClusterItem offsetItem = new MyClusterItem(location.latitude, location.longitude, key, "snippet");
                    mClusterManager.addItem(offsetItem);
                }
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
                Log.d("geoquery", "exiteddoc:" + key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("geoquery", "moveddoc:" + key);
                if (!loadedchars.contains(key)) {
                    loadedchars.add(key);
                    MyClusterItem offsetItem = new MyClusterItem(location.latitude, location.longitude, key, "snippet");
                    mClusterManager.addItem(offsetItem);
                }
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("geoquery", "ready");
            }

            @Override
            public void onGeoQueryError(Exception exception) {
                Log.d("geoquery", "dam:" + exception);
            }
        });
    }

    void cordsNotGiven(View v) {
        Intent intent = new Intent();
        intent.putExtra("locationgiven", false);
        intent.putExtra("resultingactivity", "LocatorActivity");
        setResult(RESULT_OK, intent);
        finish();
    }

    void cordsGiven(View v) {
        Intent intent = new Intent();
        intent.putExtra("locationgiven", true);
        intent.putExtra("resultingactivity", "LocatorActivity");
        intent.putExtra("latitude", mLocation.getPosition().latitude);
        intent.putExtra("longitude", mLocation.getPosition().longitude);
        Log.d("resultingmarkerposition", mLocation.getPosition().latitude + " " + mLocation.getPosition().longitude);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID //just a code
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
                getLastLocation();
            }
        }
    }

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    //gmap.setMinZoomPreference(12);
                                    LatLng ny = new LatLng(location.getLatitude(), location.getLongitude());
                                    Log.d("LocatorActivityTracker", "getlastlocation: lat, long: " + location.getLatitude() + location.getLongitude());
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    if (gmap != null) {
                                        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
                                        mLocation.setPosition(ny);
                                    }
                                    setQuery();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            //gmap.setMinZoomPreference(12);

            LatLng ny = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Log.d("LocatorActivityTracker", "locationcallback: lat, long: " + mLastLocation.getLatitude() + mLastLocation.getLongitude());
            gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    Charity clickedCharity;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng ny = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ny, 10));
        mLocation = googleMap.addMarker(new MarkerOptions().position(ny).title("Marker"));
        mLocation.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        mLocation.setZIndex(1000);
        //mLocation.set
        gmap = googleMap;
        gmap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.map_style));

        gmap.setOnCameraMoveListener(() -> {
            LatLng latLng = gmap.getCameraPosition().target;
            double metersPerPx = 1/getPixelsPerMeter(latLng.latitude, gmap.getCameraPosition().zoom);
            LinearLayout wtflayout = findViewById(R.id.wtflayout);
            double width = wtflayout.getWidth();
            double height = wtflayout.getHeight();

            geoQuery.setLocation(new GeoLocation(latLng.latitude, latLng.longitude),
                    sqrt(width*width + height*height) * metersPerPx/1000);
        });

        mClusterManager = new ClusterManager<MyClusterItem>(this, gmap);
        mClusterManager.setRenderer(new MyClusterRenderer(this, gmap,
                mClusterManager));
        gmap.setOnCameraIdleListener(mClusterManager);
        gmap.setOnMarkerClickListener(mClusterManager);
        //mClusterManager.setAnimation(true);
        mClusterManager.setOnClusterItemInfoWindowClickListener(item -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("charities").document(item.getTitle());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        clickedCharity = new Charity();
                        Log.d("Reading", "DocumentSnapshot data: " + document.getData());
                        clickedCharity.firestoreID = document.getId();
                        clickedCharity.name = item.getTitle();
                        clickedCharity.fullDescription = (String) document.get("description");
                        clickedCharity.briefDescription = clickedCharity.fullDescription.substring(0, min(clickedCharity.fullDescription.length(), 50));
                        clickedCharity.photourl = (String) document.get("photourl");
                        clickedCharity.paymentUrl = document.getString("qiwiurl");

                        Intent intent = new Intent(context, CharityActivity.class);
                        intent.putExtra("firestoreID", clickedCharity.firestoreID);
                        intent.putExtra("chname", clickedCharity.name);
                        intent.putExtra("bdesc", clickedCharity.briefDescription);
                        intent.putExtra("fdesc", clickedCharity.fullDescription);
                        intent.putExtra("url", clickedCharity.photourl);
                        intent.putExtra("qiwiPaymentUrl", clickedCharity.paymentUrl);
                        startActivity(intent);
                    } else {
                        Log.d("Reading", "No such document");
                    }
                } else {
                    Log.d("Reading", "get failed with ", task.getException());
                }
            });
        });
    }

    private static final double EARTH_CIRCUMFERENCE_METERS = 40075000;

    public double getPixelsPerMeter(double lat, double zoom) {
        double pixelsPerTile = 256 * ((double)context.getResources().getDisplayMetrics().densityDpi / 160);
        double numTiles = Math.pow(2,zoom);
        double metersPerTile = Math.cos(Math.toRadians(lat)) * EARTH_CIRCUMFERENCE_METERS / numTiles;
        return pixelsPerTile / metersPerTile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLastLocation();
        mapView.onResume();
        myGlobals.setSelectedItem(this, bottomNavigationView);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void drawCircle(LatLng point, double rad){
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);
        // Radius of the circle
        circleOptions.radius(rad);
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        // Border width of the circle
        circleOptions.strokeWidth(2);
        // Adding the circle to the GoogleMap
        gmap.addCircle(circleOptions);
    }
}
