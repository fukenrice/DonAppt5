package com.example.donappt5.PopupActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donappt5.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LocatorActivity extends AppCompatActivity implements OnMapReadyCallback {
    Button btnCancel;
    Button btnGiveGeo;
    double gotlongitude;
    double gotlatitude;
    MapView mapView;
    private GoogleMap gmap;
    int PERMISSION_ID = 3575;
    FusedLocationProviderClient mFusedLocationClient;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private Marker mLocation;
    ImageButton imgbtnCancel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);
        gotlongitude = 0;
        gotlatitude = 0;
        Intent intent = getIntent();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        String extra;
        btnCancel = findViewById(R.id.btnSkip);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cordsNotGiven(v);
            }
        });
        extra = intent.getStringExtra("btncancel");
        if (extra != null) {
            btnCancel.setText(extra);
        }
        btnGiveGeo = findViewById(R.id.btnGiveGeo);
        btnGiveGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cordsGiven(v);
            }
        });
        extra = intent.getStringExtra("btnaccept");
        if (extra != null) {
            btnGiveGeo.setText(extra);
        }
        imgbtnCancel = findViewById(R.id.imgbtnCancel);
        imgbtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cordsNotGiven(v);
            }
        });
        TextView tvHeader = findViewById(R.id.tvMapHeader);
        extra = intent.getStringExtra("headertext");
        if (extra != null) {
            tvHeader.setText(extra);
        }
        mapView = findViewById(R.id.map_view);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

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
                                    gotlatitude = location.getLatitude();
                                    gotlongitude = location.getLongitude();
                                    if (gmap != null) {
                                        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
                                        mLocation.setPosition(ny);
                                    }
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
            gotlatitude = mLastLocation.getLatitude();
            gotlongitude = mLastLocation.getLongitude();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setMinZoomPreference(10);
        LatLng ny = new LatLng(gotlatitude, gotlongitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ny));
        mLocation = googleMap.addMarker(new MarkerOptions().position(ny).title("Marker"));
        mLocation.setDraggable(true);
        gmap = googleMap;
        gmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mLocation.setPosition(point);
            }
        });
        googleMap.setMinZoomPreference(5);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLastLocation();
        mapView.onResume();
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
}
