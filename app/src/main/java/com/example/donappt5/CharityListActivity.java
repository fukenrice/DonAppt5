package com.example.donappt5;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donappt5.PopupActivities.ActivityConfirm;
import com.example.donappt5.PopupActivities.LocatorActivity;
import com.example.donappt5.helpclasses.Charity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.koalap.geofirestore.GeoFire;
import com.koalap.geofirestore.GeoLocation;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static java.lang.Math.min;

//import com.google.firebase.analytics.FirebaseAnalytics;
//TODO in general: change support mail in firebase console settings AND project name
public class CharityListActivity extends AppCompatActivity {
    private int preLast;
    ArrayList<Charity> chars = new ArrayList<Charity>();
    CharityAdapter charAdapter;
    Context ctx;
    private Toolbar mTopToolbar;
    //private FirebaseAnalytics mFirebaseAnalytics;
    private DrawerLayout drawerlayout;
    private ActionBarDrawerToggle actionbartoggle;
    private NavigationView navigationview;
    private DatabaseReference mDataBase;
    FirebaseFirestore mFirestore;
    Query mQuery;
    int totalcharitiesloaded;
    SwipeRefreshLayout pullToRefresh;
    DocumentSnapshot lastVisible;
    private int prelast;
    boolean fillingData = false;

    double latitude = -1000;
    double longitude = -1000;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        Log.i("ProgressTracker", "position 0");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charitylist);
        ctx = this;

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                charAdapter.objects = new ArrayList<Charity>();
                charAdapter.notifyDataSetChanged();
                lastVisible = null;
                fillingData = false;
                fillData();
                pullToRefresh.setRefreshing(false);
            }
        });

        Log.i("ProgressTracker", "position 1");
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        Log.i("ProgressTracker", "position 2");
        setSupportActionBar(mTopToolbar);
        Log.i("ProgressTracker", "position 3");
        // создаем адаптер
        charAdapter = new CharityAdapter(this, chars);
        fillData();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // настраиваем список
        ListView lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setClickable(true);
        lvMain.setAdapter(charAdapter);
        lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                onMyScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Charity clickedCharity = charAdapter.getCharity(position);
                Log.d("G", "itemClick: position = " + position + ", id = "
                        + id + ", name = " + clickedCharity.name + "url = " + clickedCharity.photourl);

                Intent intent = new Intent(ctx, CharityActivity.class);
                intent.putExtra("chname", clickedCharity.name);
                intent.putExtra("bdesc", clickedCharity.briefDescription);
                intent.putExtra("fdesc", clickedCharity.fullDescription);
                intent.putExtra ("trust", clickedCharity.trust);
                intent.putExtra   ("image", clickedCharity.image);
                intent.putExtra   ("id", clickedCharity.id);
                intent.putExtra   ("url", clickedCharity.photourl);
                startActivity(intent);
            }
        });

        setupNavDrawer();

        testUserHavingLocationsOfInterest();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("gettingdevicetoken", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        Log.d("gettingdevicetokem", token);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> device_token = new HashMap<String, Object>();

                        device_token.put("device_token", token);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        db.collection("users").document(user.getUid()).update(device_token);
                    }
                });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> message = new HashMap<String, Object>();
        message.put("WORKGODDAMIT", "please?");
        db.collection("Notifications").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(message);


    }

    String photourlfromstore;

    void setupNavDrawer() {
        drawerlayout = (DrawerLayout) findViewById(R.id.activity_charitylist);
        actionbartoggle = new ActionBarDrawerToggle(this, drawerlayout, R.string.Open, R.string.Close);

        drawerlayout.addDrawerListener(actionbartoggle);
        actionbartoggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationview = (NavigationView) findViewById(R.id.nv);
        navigationview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.account:
                        Toast.makeText(CharityListActivity.this, "My Account", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx, ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.settings:
                        Toast.makeText(CharityListActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(ctx, SettingsActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.create:
                        Toast.makeText(CharityListActivity.this, "Create Charity", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(ctx, CharityCreationActivity.class);
                        startActivity(intent1);
                        break;
                    default:
                        return true;
                }
                return true;

            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        View header = navigationview.getHeaderView(0);
        final ImageView ivinHeader = header.findViewById(R.id.nav_header_imageView);
        TextView tvinHeader = header.findViewById(R.id.nav_header_textView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    photourlfromstore = document.getString("photourl");
                    Picasso.with(ctx).load(photourlfromstore).fit().into(ivinHeader);
                } else {
                    Log.d("fuck", "get failed with ", task.getException());
                }
            }
        });

        if (user != null) {
            if (photourlfromstore != null) {
                Picasso.with(ctx).load(photourlfromstore).fit().into(ivinHeader);
            } else {
                if (user.getPhotoUrl() != null) {
                    //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
                    Picasso.with(ctx).load(user.getPhotoUrl().toString()).fit().into(ivinHeader);
                }
            }
            tvinHeader.setText(user.getDisplayName());
        }
    }
            // генерируем данные для адаптера
    void fillData() {
        if(fillingData) return;
        fillingData = true;
        if (lastVisible!= null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("charities")
            .startAfter(lastVisible)
            .limit(20)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    int i = 0;
                    if (documentSnapshots.size() == 0) return;
                    lastVisible = documentSnapshots.getDocuments()
                    .get(documentSnapshots.size() - 1);

                    for (QueryDocumentSnapshot document : documentSnapshots) {
                        Log.d("CharitylistLog", document.getId() + " => " + document.getData());
                        String name = document.getString("name");
                        String desc = document.getString("description");
                        String url = document.getString("photourl");
                        Log.d("CharitylistLog", "recieved: " + name + " " + desc + " " + url);
                        charAdapter.objects.add(new Charity(name, desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i, url));
                        charAdapter.notifyDataSetChanged();
                        i++;
                    }
                    fillingData = false;
                }
            });//*/
        }

        else {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("charities")
        .limit(20)
        .get()
        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                int i = 0;
                lastVisible = documentSnapshots.getDocuments()
                        .get(documentSnapshots.size() - 1);

                for (QueryDocumentSnapshot document : documentSnapshots) {
                    Log.d("CharitylistLog", document.getId() + " => " + document.getData());
                    String name = document.getString("name");
                    String desc = document.getString("description");
                    String url = document.getString("photourl");
                    Log.d("CharitylistLog", "recieved: " + name + " " + desc + " " + url);
                    charAdapter.objects.add(new Charity(name, desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i, url));
                    charAdapter.notifyDataSetChanged();
                    i++;
                }
                fillingData = false;
            }
            });
        }
        charAdapter.notifyDataSetChanged();
        //chars.add(new Charity(recievedCharities.elementAt(0).name, recievedCharities.elementAt(0).name, "wha?", -1, R.drawable.ic_launcher_foreground, -1));
        }

        void testUserHavingLocationsOfInterest() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).collection("locations")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("locationsofinterest", "size : " + String.valueOf(task.getResult().size()));
                    Log.d("locationsofinterest", "recievd : " + task.getResult());
                    if (task.getResult().size() > 0) {
                    for (DocumentSnapshot document : task.getResult()) {
                        //something
                        }
                    } else {
                    Intent intent = new Intent(ctx, ActivityConfirm.class);

                    intent.putExtra("CancelButtonTitle", "Cancel setting location");
                    intent.putExtra("ConfirmButtonTitle", "Set location");
                    intent.putExtra("PopupText", "You seem not to have locations of interest. \n" +
                    "Location of interest is a place, you are interested of hearing about, such as your local community, your city, your workplace or your district. \n" +
                    "When near a charity is registered near your location of interest, you will recieve a notification.\n" +
                    "We recommend setting a location of interest. You will be able to delete it or turn off notifications any time.");

                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    intent.putExtra("width", (int)((double)(size.x) * 0.9));
                    intent.putExtra("height", (int)((double)(size.y) * 0.7));

                    startActivityForResult(intent, 3);
                    }
                    } else {
                    Log.d("locationsofinterest", "Error getting documents: ", task.getException());

                    Intent intent = new Intent(ctx, ActivityConfirm.class);

                    intent.putExtra("CancelButtonTitle", "Cancel setting location");
                    intent.putExtra("ConfirmButtonTitle", "Set location");
                    intent.putExtra("PopupText", "You seem not to have locations of interest. \n" +
                    "Location of interest is a place, you are interested of hearing about, such as your local community, your city, your workplace or your district. \n" +
                    "When a charity is registered near your location of interest, you will recieve a notification.\n" +
                    "We recommend setting a location of interest. You will be able to delete it or turn off notifications any time.\n" +
                    "Please, do not set location of your home or workplace precisely, select public location nearby instead");

                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    intent.putExtra("width", (int)((double)(size.x) * 0.9));
                    intent.putExtra("height", (int)((double)(size.y) * 0.7));

                    startActivityForResult(intent, 3);
                }
            }
        });//*/
    }

public void onMyScroll(AbsListView lw, final int firstVisibleItem,
final int visibleItemCount, final int totalItemCount) {

        switch(lw.getId())
        {
        case R.id.lvMain:

        final int lastItem = firstVisibleItem + visibleItemCount;

        if(lastItem == totalItemCount) {
                if(preLast!=lastItem) {
                //to avoid multiple calls for last item
                Log.d("Last", "Last");
                preLast = lastItem;
                fillData();
                }
        }
        }
    }


public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("ProgressTracker", "position a");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();    switch(itemId) {
            // Android home
            case android.R.id.home:
                drawerlayout.openDrawer(GravityCompat.START);
                return true;      // manage other entries if you have it ...
            case R.id.action_search:
                Toast.makeText(CharityListActivity.this, "Menu action clicked", Toast.LENGTH_LONG).show();
                return true;
        }    return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {return;}

        String resultingactivity = data.getStringExtra("resultingactivity");
        Log.d("progresstracker", "resulted activity " + resultingactivity);
        if (resultingactivity != null) {
            if (resultingactivity.equals("LocatorActivity")) {
                onLocatorActivityResult(requestCode, resultCode, data);
            } else {
                if (resultingactivity.equals("ActivityConfirm")) {
                    String result = data.getStringExtra("result");
                    if (result.equals("confirmed")) {
                        Log.d("progresstracker", "confirmedresult");
                        Intent intent = new Intent(ctx, LocatorActivity.class);
                        intent.putExtra("headertext", "Set a location of interest. Hold on the marker and drag it.");
                        intent.putExtra("btnaccept", "Here");
                        intent.putExtra("btncancel", "Skip this step");
                        startActivityForResult(intent, 3);
                    }
                }
            }
        }
    }

    protected void onLocatorActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {return;}

        boolean coordsgiven = data.getBooleanExtra("locationgiven", false);
        latitude = data.getDoubleExtra("latitude", -1000);
        longitude = data.getDoubleExtra("longitude", -1000);

        if (coordsgiven && (latitude > -900)) {
            Toast.makeText(ctx, "lat: " + latitude + " long: " + longitude, Toast.LENGTH_LONG).show();
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> location = new HashMap<String, Object>();
            location.put("latitude", latitude);
            location.put("longitude", longitude);

            db.collection("users").document(user.getUid()).collection("locations").document("FirstLocation").set(location)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            CollectionReference colref = FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("locations");
                            GeoFire geoFirestore = new GeoFire(colref);
                            geoFirestore.setLocation("FirstLocation", new GeoLocation(latitude, longitude));
                        }
                    });
            db.collection("userlocations").document(user.getUid()).set(new HashMap<String, Object>())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            CollectionReference colref = FirebaseFirestore.getInstance().collection("userlocations");
                            GeoFire geoFirestore = new GeoFire(colref);
                            geoFirestore.setLocation(user.getUid(), new GeoLocation(latitude, longitude));
                        }
                    });
        }
        else {
            Toast.makeText(ctx, "coordinates not given", Toast.LENGTH_SHORT).show();
        }
    }
}