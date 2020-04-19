package com.example.donappt5.helpclasses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.donappt5.AuthenticationActivity;
import com.example.donappt5.CharityCreationActivity;
import com.example.donappt5.CharityListActivity;
import com.example.donappt5.ProfileActivity;
import com.example.donappt5.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class BaseActivity extends AppCompatActivity {

    public Toolbar toolbar;                              // Declaring the Toolbar Object

    private DrawerLayout drawerlayout;
    private ActionBarDrawerToggle actionbartoggle;
    private NavigationView navigationview;

    ActionBarDrawerToggle mDrawerToggle;
    Context context;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private androidx.appcompat.widget.Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean useToolbar() {
        return true;
    }


    @Override
    public void setContentView(int layoutResID) {
        context = this;
        //getLayoutInflater().inflate(layoutResID, );
        mTopToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupNavDrawer();
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    String photourlfromstore;
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
                        Toast.makeText(BaseActivity.this, "My Account",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.settings:
                        Toast.makeText(BaseActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(context, AuthenticationActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.create:
                        Toast.makeText(BaseActivity.this, "Create Charity",Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(context, CharityCreationActivity.class);
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
                    Picasso.with(context).load(photourlfromstore).fit().into(ivinHeader);
                } else {
                    Log.d("fuck", "get failed with ", task.getException());
                }
            }
        });

        if(user != null) {
            if (photourlfromstore != null) {
                Picasso.with(context).load(photourlfromstore).fit().into(ivinHeader);
            }
            else { if (user.getPhotoUrl() != null) {
                //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
                Picasso.with(context).load(user.getPhotoUrl().toString()).fit().into(ivinHeader);
            } }
            tvinHeader.setText(user.getDisplayName());
        }
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
                Toast.makeText(BaseActivity.this, "Menu action clicked", Toast.LENGTH_LONG).show();
                return true;
        }    return super.onOptionsItemSelected(item);
    }
}