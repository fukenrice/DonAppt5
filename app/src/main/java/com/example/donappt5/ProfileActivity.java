package com.example.donappt5;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout drawerlayout;
    private ActionBarDrawerToggle actionbartoggle;
    private NavigationView navigationview;
    private Toolbar mTopToolbar;
    Button btnLogOut;
    Context ctx;
    ImageView ivProfile;
    int SELECT_PICTURE = 12341;
    String pathtoimage;
    Uri loadedUri;
    Button btnLoadProfile;
    String fileUrl;
    String photourlfromstore;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);
        ctx = this;
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);

        setupNavDrawer();
        btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(ctx)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                Intent intent = new Intent(ctx, AuthenticationActivity.class);
                                startActivity(intent);
                            }
                        });
            }
        });

        ivProfile = findViewById(R.id.ivProfilePhoto);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    photourlfromstore = document.getString("photourl");
                    Picasso.with(ctx).load(photourlfromstore).fit().into(ivProfile);
                } else {
                    Log.d("fuck", "get failed with ", task.getException());
                }
            }
        });

        if (photourlfromstore != null) {
            Picasso.with(ctx).load(photourlfromstore).fit().into(ivProfile);
        }
        else {if (user.getPhotoUrl() != null) {
            //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
            Picasso.with(ctx).load(user.getPhotoUrl().toString()).fit().into(ivProfile);
        }}

        LinearLayout llwithimage = findViewById(R.id.llImage);
        llwithimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage();
            }
        });
        btnLoadProfile = findViewById(R.id.btnLoadProfile);
        btnLoadProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    void uploadImage() {
        if (pathtoimage != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference();

            Uri file = loadedUri;//Uri.fromFile(new File(pathtoimage));

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            StorageReference imgsref = storageRef.child("users/" + user.getUid() + "/photo");
            UploadTask uploadTask = imgsref.putFile(file);

            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            storageRef.child("users/" + user.getUid() + "/photo").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Log.d("urlgetter", uri.toString());
                    fileUrl = uri.toString();
                    Map<String, Object> hmap = new HashMap<>();
                    hmap.put("photourl", fileUrl);
                    Log.d("puttingphoto", "url: " + fileUrl);
                    db.collection("users")
                            .document(user.getUid())
                            .update(hmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                }
            });
            //Log.d("urlgetter", fileUrl);
        }
    }

    void loadImage() {
        openImageChooser();
    }

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }


    void setupNavDrawer() {
        drawerlayout = (DrawerLayout)findViewById(R.id.activity_layout_profile);
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
                        Toast.makeText(ProfileActivity.this, "My Account",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx, ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.settings:
                        Toast.makeText(ProfileActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(ctx, SettingsActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.create:
                        Toast.makeText(ProfileActivity.this, "Create Charity",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ProfileActivity.this, "Menu action clicked", Toast.LENGTH_LONG).show();
                return true;
        }    return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {return;}

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
                                ivProfile.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ivProfile.setImageURI(selectedImageUri);
                                        loadedUri = selectedImageUri;
                                    }
                                });
                            }
                        }
                    }
                }
            }).start();
    }

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
}
