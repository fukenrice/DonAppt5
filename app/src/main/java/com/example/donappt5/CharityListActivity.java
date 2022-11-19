package com.example.donappt5;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.donappt5.PopupActivities.ActivityConfirm;
import com.example.donappt5.PopupActivities.LocatorActivity;
import com.example.donappt5.helpclasses.Charity;
import com.example.donappt5.helpclasses.MyGlobals;
//import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.koalap.geofirestore.GeoFire;
import com.koalap.geofirestore.GeoLocation;
import com.koalap.geofirestore.GeoQuery;
import com.koalap.geofirestore.GeoQueryEventListener;
//import com.squareup.picasso.Picasso;
//import com.stripe.Stripe;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentIntent;
//import com.stripe.exception.StripeException;
//import com.stripe.model.PaymentIntent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    ArrayList<String> geochars = new ArrayList<String>();
    CharityAdapter charAdapter;
    Context ctx;
    private Toolbar mTopToolbar;
    //private FirebaseAnalytics mFirebaseAnalytics;
    SwipeRefreshLayout pullToRefresh;
    DocumentSnapshot lastVisible;
    private int prelast;
    boolean fillingData = false;
    public static int FILLING_ALPHABET = 0, FILLING_DISTANCE = 1, FILLING_SEARCH = 2, FILLING_FAVORITES = 3;
    int fillingmode = 0;
    int fdistance = 0;
    double latitude = -1000;
    double longitude = -1000;
    MyGlobals myGlobals;
    String queryInput;
    GeoQuery fillingQuery;
    String tag = "none";

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        Log.i("ProgressTracker", "position 0");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charitylist);
        ctx = this;

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_GSMF14GK1NPKphtwTYRYl60W0083LGv2jw"
        );

        setupGooglePay();
        handleIntent(getIntent());

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fillingmode = FILLING_ALPHABET;
                fdistance = 0;
                fillingQuery = null;
                charAdapter.objects = new ArrayList<Charity>();
                charAdapter.notifyDataSetChanged();
                queryInput = null;
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
                Log.d("Click", "itemClick: position = " + position + ", id = "
                        + id + ", name = " + clickedCharity.name + "url = " + clickedCharity.photourl + ", payment url = " + clickedCharity.paymentUrl);

                Intent intent = new Intent(ctx, CharityActivity.class);
                intent.putExtra("chname", clickedCharity.name);
                intent.putExtra("bdesc", clickedCharity.briefDescription);
                intent.putExtra("fdesc", clickedCharity.fullDescription);
                intent.putExtra("trust", clickedCharity.trust);
                intent.putExtra("image", clickedCharity.image);
                intent.putExtra("id", clickedCharity.id);
                intent.putExtra("url", clickedCharity.photourl);
                intent.putExtra("qiwiPaymentUrl", clickedCharity.paymentUrl);
                startActivity(intent);
            }
        });

        myGlobals = new MyGlobals(ctx);
        myGlobals.setupNavDrawer(ctx, this, findViewById(R.id.activity_charitylist));

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
        MyTask mt = new MyTask();
        mt.execute();
    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("AsyncTask", "Begin");
        }

        @Override
        protected Void doInBackground(Void... params) {
            testStripe();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d("AsyncTask", "End");
        }
    }


    void testStripe() {

        //Log.d("stripe", Stripe.apiKey);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("amount", 1000);
        params.put("currency", "usd");
        ArrayList paymentMethodTypes = new ArrayList();
        paymentMethodTypes.add("card");
        Log.d("stripe", "step 1");
        params.put("payment_method_types", paymentMethodTypes);
        params.put("receipt_email", "jenny.rosen@example.com");


    }

    void doMySearch(String querys) {
        Log.d("searchfunction", "input = " + querys);
        queryInput = querys;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("charities").orderBy("name").startAt(querys).endAt(querys + "\uf8ff").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        int i = 0;
                        for (QueryDocumentSnapshot document : documentSnapshots) {
                            ++i;
                            Log.d("CharitylistLog", document.getId() + " => " + document.getData());
                            String name = document.getString("name");
                            String desc = document.getString("description");
                            String url = document.getString("photourl");
                            Log.d("CharitylistLog", "recieved: " + name + " " + desc + " " + url);
                            charAdapter.objects.add(new Charity(name, desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i, url));
                            charAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onSearchRequested() {
        doMySearch("asearch");
        return super.onSearchRequested();
    }


    private void handleIntent(Intent intent) {
        Log.d("searchfunction", "intent: " + Intent.ACTION_SEARCH + " " + intent.getAction());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("searchfunction", "search");
            doMySearch(query);
        }
        String gottag = intent.getStringExtra("tagclicked");
        if (gottag != null) {
            tag = gottag;
        }
        boolean fillingfavorites = intent.getBooleanExtra("fillingfavorites", false);
        if (fillingfavorites) {
            Log.d("fillingmode", "favorites intent");
            fillingmode = FILLING_FAVORITES;
        }
    }

    void fillData() {
        Log.d("fillingmode", String.valueOf(fillingmode));
        if (fillingmode == FILLING_ALPHABET) {
            fillAllData();
        } else if (fillingmode == FILLING_SEARCH) {
            return;
        } else if (fillingmode == FILLING_DISTANCE) {
            fillDistanceData();
        } else if (fillingmode == FILLING_FAVORITES) {
            fillFavoritesData();
        }
    }

    void fillFavoritesData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users").document(user.getUid()).collection("favorites")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        int i = 0;
                        if (documentSnapshots.size() == 0) return;

                        for (QueryDocumentSnapshot document : documentSnapshots) {
                            Log.d("CharitylistLog", document.getId() + " => " + document.getData());
                            String name = document.getString("name");
                            String desc = document.getString("description");
                            String url = document.getString("photourl");
                            String qiwiPaymentUrl = document.getString("qiwiurl");
                            Log.d("CharitylistLog", "recieved: " + name + " " + desc + " " + url);
                            charAdapter.objects.add(new Charity(name, (desc == null) ? "" : desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i, url, qiwiPaymentUrl));
                            charAdapter.notifyDataSetChanged();
                            i++;
                        }
                        fillingData = false;
                    }
                });//*/
    }

    void fillDistanceData() {
        if (fdistance != 0) {
            fillingQuery.setRadius(fdistance + 100);
            fdistance += 100;
        } else {
            fdistance += 100;
            CollectionReference ref = FirebaseFirestore.getInstance().collection("charitylocations");
            GeoFire geoFireuserlocation = new GeoFire(ref);
            fillingQuery = geoFireuserlocation.queryAtLocation(new GeoLocation(latitude, longitude), fdistance);
            fillingQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                    Log.d("geoquery", "entereddoc:" + key);
                    //Marker newmarker = gmap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(key));;
                    //loadedmarkers.add(newmarker);
                    if (!geochars.contains(key)) {
                        Charity enteredCharity = new Charity();
                        FirebaseFirestore.getInstance().collection("charities").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                enteredCharity.name = key;
                                enteredCharity.fullDescription = (String) documentSnapshot.get("description");
                                enteredCharity.briefDescription = enteredCharity.fullDescription.substring(0, min(enteredCharity.fullDescription.length(), 50));
                                enteredCharity.photourl = (String) documentSnapshot.get("photourl");
                                enteredCharity.paymentUrl = (String) documentSnapshot.get("qiwiurl");
                                Location chloc = new Location("");
                                chloc.setLatitude(location.latitude);
                                chloc.setLongitude(location.longitude);
                                Location userloc = new Location("");
                                userloc.setLatitude(latitude);
                                userloc.setLongitude(longitude);
                                enteredCharity.trust = chloc.distanceTo(userloc);
                                //double doclatitude, doclongitude;
                                charAdapter.objects.add(enteredCharity);
                                charAdapter.notifyDataSetChanged();
                            }
                        });
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
                    //if (!loadedchars.contains(key)) {
                    //    loadedchars.add(key);
                    //    MyClusterItem offsetItem = new MyClusterItem(location.latitude, location.longitude, key, "snippet");
                    //    mClusterManager.addItem(offsetItem);
                    //}
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
    }

    // генерируем данные для адаптера
    void fillAllData() {
        if (queryInput != null) return;
        if (fillingData) return;
        fillingData = true;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query taggedquery;
        if (tag != "none") {
            taggedquery = db.collection("charities").whereEqualTo(tag, true);
        } else {
            taggedquery = db.collection("charities");
        }

        if (lastVisible != null) {
            taggedquery
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
                                String qiwiPaymentUrl = document.getString("qiwiurl");
                                Log.d("CharitylistLog", "recieved: " + name + " " + desc + " " + url + " " + qiwiPaymentUrl);
                                charAdapter.objects.add(new Charity(name, desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i, url, qiwiPaymentUrl));
                                charAdapter.notifyDataSetChanged();
                                i++;
                            }
                            fillingData = false;
                        }
                    });//*/
        } else {
            taggedquery
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
                                String qiwiPaymentUrl = document.getString("qiwiurl");
                                Log.d("CharitylistLog", "recieved: " + name + " " + desc + " " + url + " " + qiwiPaymentUrl);
                                charAdapter.objects.add(new Charity(name, desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i, url, qiwiPaymentUrl));
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
                                    latitude = (double) document.get("latitude");
                                    longitude = (double) document.get("longitude");
                                }
                            } else {
                                Intent intent = new Intent(ctx, ActivityConfirm.class);

                                intent.putExtra("CancelButtonTitle", "Cancel setting location");
                                intent.putExtra("ConfirmButtonTitle", "Set location");
                                intent.putExtra("PopupText", "You seem not to have locations of interest." +
                                        "Location of interest is a place you are interested in hearing about, such as your local community, your city or your district." +
                                        "When a charity is registered near your location of interest you recieve a notification." +
                                        "We recommend setting a location of interest. You will be able to delete it or turn off notifications at any time.");

                                Display display = getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                intent.putExtra("width", (int) ((double) (size.x) * 0.9));
                                intent.putExtra("height", (int) ((double) (size.y) * 0.7));

                                startActivityForResult(intent, 3);
                            }
                        } else {
                            Log.d("locationsofinterest", "Error getting documents: ", task.getException());

                            Intent intent = new Intent(ctx, ActivityConfirm.class);

                            intent.putExtra("CancelButtonTitle", "Cancel setting location");
                            intent.putExtra("ConfirmButtonTitle", "Set location");
                            intent.putExtra("PopupText", "You seem not to have locations of interest." +
                                    "Location of interest is a place you are interested in hearing about, such as your local community, your city or your district." +
                                    "When a charity is registered near your location of interest you recieve a notification." +
                                    "We recommend setting a location of interest. You will be able to delete it or turn off notifications at any time.");

                            Display display = getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            intent.putExtra("width", (int) ((double) (size.x) * 0.9));
                            intent.putExtra("height", (int) ((double) (size.y) * 0.7));

                            startActivityForResult(intent, 3);
                        }
                    }
                });//*/

    }

    void setupGooglePay() {

    }


    public void onMyScroll(AbsListView lw, final int firstVisibleItem,
                           final int visibleItemCount, final int totalItemCount) {
        if (fillingmode == FILLING_FAVORITES) return;
        switch (lw.getId()) {
            case R.id.lvMain:
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) {
                        //to avoid multiple calls for last item
                        Log.d("Last", "Last");
                        preLast = lastItem;
                        fillData();
                        Log.d("georad", String.valueOf(fdistance));
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            // Android home
            case android.R.id.home:
                myGlobals.drawerlayout.openDrawer(GravityCompat.START);
                return true;      // manage other entries if you have it ...
            case R.id.action_search:
                Toast.makeText(CharityListActivity.this, "Menu action clicked", Toast.LENGTH_LONG).show();
                //onSearchRequested();
                return true;
            case R.id.action_sortbydistance:
                Toast.makeText(ctx, "feature not yet ready", Toast.LENGTH_SHORT).show();
                break; //TODO sort by distance
            //fillingmode = FILLING_DISTANCE;
            //charAdapter.objects = new ArrayList<Charity>();
            //charAdapter.notifyDataSetChanged();
            //queryInput = null;
            //lastVisible = null;
            //fillingData = false;
            //fillData();
            //pullToRefresh.setRefreshing(false);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

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
        if (data == null) {
            return;
        }

        boolean coordsgiven = data.getBooleanExtra("locationgiven", false);
        latitude = data.getDoubleExtra("latitude", -1000);
        longitude = data.getDoubleExtra("longitude", -1000);

        if (coordsgiven && (latitude > -900)) {
            if (fillingQuery != null) {
                fillingQuery.setLocation(new GeoLocation(latitude, longitude), fillingQuery.getRadius());
            }
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
            db.collection("userlocations").document(user.getUid()).set(location)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            CollectionReference colref = FirebaseFirestore.getInstance().collection("userlocations");
                            GeoFire geoFirestore = new GeoFire(colref);
                            geoFirestore.setLocation(user.getUid(), new GeoLocation(latitude, longitude));
                        }
                    });
            db.collection("users").document(user.getUid()).update(location);
        } else {
            Toast.makeText(ctx, "coordinates not given", Toast.LENGTH_SHORT).show();
        }
    }
}