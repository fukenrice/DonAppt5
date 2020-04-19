package com.example.donappt5;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donappt5.CharityDescriptionFragments.CharityDescFragment;
import com.example.donappt5.CharityDescriptionFragments.CharityGoalsFragment;
import com.example.donappt5.helpclasses.Charity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CharityActivity extends AppCompatActivity {
    Charity descChar;
    Context ctx;

    private GestureDetector gestureDetector;

    CharityDescFragment fragdesc;
    CharityGoalsFragment fraggoal;
    float lastX;
    static final int PAGE_COUNT = 10;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager pager;
    PagerAdapter pagerAdapter;
    private Toolbar mTopToolbar;

    private DrawerLayout drawerlayout;
    private ActionBarDrawerToggle actionbartoggle;
    private NavigationView navigationview;

    public void onCreate (Bundle savedInstanceState) {
        ctx = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charitydesc);
        Log.i("ActivityTrack", "entered carityactivity");
        Intent intent = getIntent();

        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);

        fragdesc = new CharityDescFragment();
        fraggoal = new CharityGoalsFragment();

        descChar = new Charity(intent.getStringExtra("chname"),
                               intent.getStringExtra("bdesc"),
                               intent.getStringExtra("fdesc"),
                               intent.getFloatExtra ("trust", 0),
                               intent.getIntExtra   ("image", 0),
                               intent.getIntExtra   ("id", -1),
                               intent.getStringExtra("url"));
        TextView tvName = (TextView) findViewById(R.id.tvName);
        ImageView ivImage = (ImageView) findViewById(R.id.ivImage);
        if (descChar.photourl != null) {
            ivImage.setImageResource(R.drawable.ic_sync);
            //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
            Picasso.with(ctx).load(descChar.photourl).fit().into(ivImage);
            //new DownloadImageTask(ivImage)
            //        .execute("https://firebasestorage.googleapis.com/v0/b/donapp-d2378.appspot.com/o/images%2Fimage%3A96754?alt=media&token=3a4efe33-e1b1-43d7-94ec-7731105d5799");
            //Log.d("urlnotnull", "setting image");
        }
        //TextView tvDesc = (TextView) findViewById(R.id.tvFullDesc);
        TextView tvRating = (TextView) findViewById(R.id.tvTrustRating);

        tvName.setText(descChar.name);
        //TODO tvImage.setImage or loading image by url
        //tvDesc.setText(descChar.fullDescription);
        tvRating.setText(String.valueOf(descChar.trust));//*/

        pager = (ViewPager) findViewById(R.id.ViewPager);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d("TAG", "onPageSelected, position = " + position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });//*/

        setupNavDrawer();
    }

    String photourlfromstore;

    void setupNavDrawer() {
        drawerlayout = (DrawerLayout)findViewById(R.id.activity_charitydesc);
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
                        Toast.makeText(CharityActivity.this, "My Account",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx, ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.settings:
                        Toast.makeText(CharityActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(ctx, AuthenticationActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.create:
                        Toast.makeText(CharityActivity.this, "Create Charity",Toast.LENGTH_SHORT).show();
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

        if(user != null) {
            if (photourlfromstore != null) {
                Picasso.with(ctx).load(photourlfromstore).fit().into(ivinHeader);
            }
            else { if (user.getPhotoUrl() != null) {
                //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
                Picasso.with(ctx).load(user.getPhotoUrl().toString()).fit().into(ivinHeader);
            } }
            tvinHeader.setText(user.getDisplayName());
        }
    }


    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return CharityDescFragment.newInstance(descChar);
                case 1: return CharityGoalsFragment.newInstance(descChar);
                default: return CharityDescFragment.newInstance(descChar);
            }
        }

        @Override
        public int getCount() {
            return 2;
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
                Toast.makeText(CharityActivity.this, "Menu action clicked", Toast.LENGTH_LONG).show();
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




    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN: {
                lastX = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = event.getX();

                // if left to right swipe on screen
                if (lastX < currentX - 250) {
                    tabHost.setCurrentTab(tabHost.getCurrentTab() - 1);
                }

                // if right to left swipe on screen
                if (lastX > currentX + 250) {
                    tabHost.setCurrentTab(tabHost.getCurrentTab() + 1);
                }

                break;
            }
        }
        return false;
    }
    tabHost.addTab(tabSpec);

    Intent callintent2 = new Intent(this, CharityGoalsFragment.class);

        callintent2.putExtra("chname", descChar.name);
        callintent2.putExtra("bdesc", descChar.briefDescription);
        callintent2.putExtra("fdesc", descChar.fullDescription);
        callintent2.putExtra("image", descChar.image);
        callintent2.putExtra("trust", descChar.trust);
        callintent2.putExtra("id", descChar.id);

    tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("Вкладка 2");
        tabSpec.setContent(callintent2);
        tabHost.addTab(tabSpec);//*/
}

