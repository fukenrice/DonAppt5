package com.example.donappt5;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donappt5.CharityCreationFragments.CharityCreateDesc;
import com.example.donappt5.CharityCreationFragments.CharityCreateGoals;
import com.example.donappt5.PopupActivities.ActivityConfirm;
import com.example.donappt5.helpclasses.Charity;
//import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import static com.google.common.primitives.Ints.min;

public class CharityCreationActivity extends AppCompatActivity {
    Context context;
    int SELECT_PICTURE = 2878;
    //private FirebaseAnalytics mFirebaseAnalytics;
    ViewPager pager;
    PagerAdapter pagerAdapter;
    private DrawerLayout drawerlayout;
    private ActionBarDrawerToggle actionbartoggle;
    private NavigationView navigationview;
    EditText etName;
    TextView tvState;
    private Toolbar mTopToolbar;
    Charity descChar;
    private GestureDetector gestureDetector;

    String pathtoimage;
    String fileUrl;

    TextView tvNameCheck;

    CharityCreateDesc fragdesc;
    CharityCreateGoals fraggoal;
    View fragdescview;
    Button btnCreate;
    double latitude = -1000;
    double longitude = -1000;
    RelativeLayout layoutImage;
    Uri loadedUri;
    ImageView imgbtnCheckName;
    ImageView imgChange;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charitycreation);
        context = this;
        Log.d("ActivityTracker", "entered CharityCreationActivity");
        etName = findViewById(R.id.etName);
        //TODO imageview
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        imgChange = findViewById(R.id.ivChangeImage);

        btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreate();
            }
        });
        fragdesc = new CharityCreateDesc();
        fraggoal = new CharityCreateGoals();
        tvState = findViewById(R.id.tvState);
        pager = findViewById(R.id.ChangePager);

        pagerAdapter = new CharityCreationActivity.MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        Log.d("ActivityTracker", "entered CharityCreationActivity2");
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
        imgbtnCheckName = findViewById(R.id.imgbtnNameCheck);
        imgbtnCheckName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkName();
            }
        });
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                onTextChangeListener();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tvNameCheck = findViewById(R.id.tvCharityNameCheck);

        layoutImage = findViewById(R.id.relLayoutImage);
        layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage();
            }
        });
    }

    void loadImage() {
        openImageChooser();
    }

    void checkName() {
        String checkingname = etName.getText().toString();
        if (checkingname.equals("")) return;
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference docIdRef = rootRef.collection("charities").document(checkingname);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("namechecker", "Document exists!");
                        imgbtnCheckName.setImageResource(R.drawable.ic_warning_foreground);
                        tvNameCheck.setText("charity with such name already exists. If you are it's owner, you can change it's contents");
                    } else {
                        Log.d("namechecker", "Document does not exist!");
                        imgbtnCheckName.setImageResource(R.drawable.ic_check_foreground);
                        tvNameCheck.setText("charity with such name does not exist. You can create one!");
                    }
                } else {
                    Log.d("namechecker", "Failed with: ", task.getException());
                }
            }
        });
    }

    void onTextChangeListener() {
        imgbtnCheckName.setImageResource(R.drawable.ic_sync);
    }

    void Flush() {
        Toast.makeText(CharityCreationActivity.this, fragdesc.getText(), Toast.LENGTH_LONG).show();
        // Access a Cloud Firestore instance from your Activity
    }

    void btnCreate() {
        //GeoFire geoFire = new GeoFire(ref); //TODO geofire???

        Intent intent = new Intent(context, LocatorActivity.class);
        intent.putExtra("headertext", "Give us location of your charity, although not mandatory, it will help raise awareness in your local community. Hold on the marker and drag it.");
        intent.putExtra("btnaccept", "We are here");
        intent.putExtra("btncancel", "Skip this step");
        startActivityForResult(intent, 1);
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
                        createCharity();
                    }
                }
            }
        }
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (resultCode == RESULT_OK) {
                        if (requestCode == SELECT_PICTURE) {
                            // Get the url from data
                            final Uri selectedImageUri = data.getData();
                            if (null != selectedImageUri) {
                                // Get the path from the Uri
                                String path = getPathFromURI(selectedImageUri);
                                pathtoimage = path;
                                Log.i("imageloader", "Image Path : " + path);
                                // Set the image in ImageView
                                imgChange.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgChange.setImageURI(selectedImageUri);
                                        loadedUri = selectedImageUri;
                                    }
                                });
                            }
                        }
                    }
                }
            }).start();
        }
    }

    void createCharity() {
        Log.d("progresstracker", "createCharity");
        final Charity creatingChar = new Charity();
        creatingChar.name = etName.getText().toString();
        creatingChar.fullDescription = fragdesc.getText();
        creatingChar.briefDescription = creatingChar.fullDescription.substring(0, min(creatingChar.fullDescription.length(), 50));
        // Access a Cloud Firestore instance from your Activity
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final Map<String, Object> charity = new HashMap<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        charity.put("name", creatingChar.name);
        charity.put("description", creatingChar.fullDescription);
        charity.put("creatorid", user.getUid());
        if (latitude > -990) {
            charity.put("latitude", latitude);
            charity.put("logitude", longitude);
        }
        if (pathtoimage != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference();

            Uri file = loadedUri;//Uri.fromFile(new File(pathtoimage));

            StorageReference imgsref = storageRef.child("charities"+creatingChar.name+"/photo");
            UploadTask uploadTask = imgsref.putFile(file);

            storageRef.child("charities"+creatingChar.name+"/photo").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Log.d("urlgetter", uri.toString());
                    fileUrl = uri.toString();
                    charity.put("photourl", fileUrl);
                    db.collection("charities").document(creatingChar.name)
                            .set(charity);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
            //Log.d("urlgetter", fileUrl);
        } else {
            db.collection("charities").document(creatingChar.name)
                    .set(charity)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Charitycreationlog", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Charitycreationlog", "Error writing document", e);
                        }
                    });
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("charities/" + creatingChar.name);
        }

    }

    protected void onLocatorActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {return;}

        boolean coordsgiven = data.getBooleanExtra("locationgiven", false);
        latitude = data.getDoubleExtra("latitude", 0);
        longitude = data.getDoubleExtra("longitude", 0);

        if (coordsgiven) {
            Toast.makeText(context, "lat: " + latitude + " long: " + longitude, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context, "coordinates not given", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(context, ActivityConfirm.class);
        intent.putExtra("CancelButtonTitle", "go back to charity creation");
        intent.putExtra("ConfirmButtonTitle", "confirm and create charity");
        intent.putExtra("PopupText", "Create charity?");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        intent.putExtra("width", (int)((double)(size.x) * 0.9));
        intent.putExtra("height", (int)((double)(size.y) * 0.5));

        startActivityForResult(intent, 2);
    }



    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return fragdesc;
                case 1: return fraggoal;
                default: return fragdesc;
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
                Toast.makeText(CharityCreationActivity.this, "Menu action clicked", Toast.LENGTH_LONG).show();
                return true;
        }    return super.onOptionsItemSelected(item);
    }

    private void handlePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2878:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            //  Show your own message here
                        } else {
                            showSettingsAlert();
                        }
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        openAppSettings(CharityCreationActivity.this);
                    }
                });
        alertDialog.show();
    }

    public static void openAppSettings(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    String photourlfromstore;

    void setupNavDrawer() {
        drawerlayout = (DrawerLayout)findViewById(R.id.activity_charitycreation);
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
                        Toast.makeText(CharityCreationActivity.this, "My Account",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.settings:
                        Toast.makeText(CharityCreationActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(context, AuthenticationActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.create:
                        Toast.makeText(CharityCreationActivity.this, "Create Charity",Toast.LENGTH_SHORT).show();
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
}

