package com.example.donappt5;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donappt5.helpclasses.Charity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
//import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;

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
                fillData();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Charity clickedCharity = charAdapter.getCharity(position);
                Log.d("G", "itemClick: position = " + position + ", id = "
                        + id + ", name = " + clickedCharity.name);

                Intent intent = new Intent(ctx, CharityActivity.class);
                intent.putExtra("chname", clickedCharity.name);
                intent.putExtra("bdesc", clickedCharity.briefDescription);
                intent.putExtra("fdesc", clickedCharity.fullDescription);
                intent.putExtra ("trust", clickedCharity.trust);
                intent.putExtra   ("image", clickedCharity.image);
                intent.putExtra   ("id", clickedCharity.id);
                startActivity(intent);
            }
        });

        setupNavDrawer();

    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();

        // Get the 50 highest rated restaurants
        mQuery = mFirestore.collection("restaurants")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(50);
    }

    void setupNavDrawer() {
        drawerlayout = (DrawerLayout)findViewById(R.id.activity_charitylist);
        actionbartoggle = new ActionBarDrawerToggle(this, drawerlayout,R.string.Open, R.string.Close);

        drawerlayout.addDrawerListener(actionbartoggle);
        actionbartoggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationview = (NavigationView)findViewById(R.id.nv);
        navigationview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.account:
                        Toast.makeText(CharityListActivity.this, "My Account",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx, ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.settings:
                        Toast.makeText(CharityListActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(ctx, AuthenticationActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.create:
                        Toast.makeText(CharityListActivity.this, "Create Charity",Toast.LENGTH_SHORT).show();
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
        ImageView ivinHeader = header.findViewById(R.id.nav_header_imageView);
        TextView tvinHeader = header.findViewById(R.id.nav_header_textView);

        if(user != null) {
            if (user.getPhotoUrl() != null) {
                //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
                new CharityListActivity.DownloadImageTask(ivinHeader)
                        .execute(user.getPhotoUrl().toString());
            }
            tvinHeader.setText(user.getDisplayName());
        }
    }

    // генерируем данные для адаптера
    void fillData() {
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
                                Log.d("CharitylistLog", "recieved: " + name + " " + desc);
                                charAdapter.objects.add(new Charity(name, desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i));
                                charAdapter.notifyDataSetChanged();
                                i++;
                            }
                        }
                    });
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
                                Log.d("CharitylistLog", "recieved: " + name + " " + desc);
                                charAdapter.objects.add(new Charity(name, desc.substring(0, min(desc.length(), 50)), desc, -1, R.drawable.ic_launcher_foreground, i));
                                charAdapter.notifyDataSetChanged();
                                i++;
                            }
                        }
                    });
        }
        charAdapter.notifyDataSetChanged();
        //chars.add(new Charity(recievedCharities.elementAt(0).name, recievedCharities.elementAt(0).name, "wha?", -1, R.drawable.ic_launcher_foreground, -1));
    }


    @Override
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}