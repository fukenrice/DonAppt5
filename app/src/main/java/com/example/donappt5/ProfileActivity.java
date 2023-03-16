package com.example.donappt5;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donappt5.helpclasses.Friend;
import com.example.donappt5.helpclasses.MyGlobals;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout drawerlayout;
    private ActionBarDrawerToggle actionbartoggle;
    Button btnLogOut;
    Context ctx;
    ImageView ivProfile;
    int SELECT_PICTURE = 12341;
    String pathtoimage;
    Uri loadedUri;
    Button btnLoadProfile;
    String fileUrl;
    String photourlfromstore;
    ListView lvFriends;
    MyGlobals myGlobals;
    FriendsAdapter friendsAdapter;
    ArrayList<Friend> friends;
    Button btnFavs;
    TextView tvUserName;
    Button btnChangeName;
    BottomNavigationView bottomNavigationView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);
        ctx = this;
        friends = new ArrayList<Friend>();

        myGlobals = new MyGlobals(ctx);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        myGlobals.setupBottomNavigation(ctx, this, bottomNavigationView);

        btnChangeName = findViewById(R.id.btnChangeName);
        btnChangeName.setOnClickListener(view -> requestNameChange());

        btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(v -> {
            LoginManager.getInstance().logOut();
            AuthUI.getInstance()
                    .signOut(ctx)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(ctx, AuthenticationActivity.class);
                        startActivity(intent);
                    });
        });

        ivProfile = findViewById(R.id.ivProfilePhoto);
        tvUserName = findViewById(R.id.tvUserName);
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
                    tvUserName.setText(document.getString("name"));
                } else {
                    Log.d("dam", "get failed with ", task.getException());
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
        btnFavs = findViewById(R.id.btnFavs);
        btnFavs.setOnClickListener(v -> onFavsClick());
//read_custom_friendlists
        manageFriendsListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        myGlobals.setSelectedItem(this, bottomNavigationView);
    }

    void requestNameChange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String ans = input.getText().toString();
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            HashMap<String, Object> update = new HashMap<>();
            update.put("name", ans);
            tvUserName.setText(ans);

            db.collection("users").document(user.getUid())
                    .update(update);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void onFavsClick() {
        Intent intent = new Intent(ctx, CharityListActivity.class);
        intent.putExtra("fillingfavorites", true);
        startActivity(intent);
    }

    void manageFriendsListView() {
        friendsAdapter = new FriendsAdapter(ctx, friends);
        MyGlobals mg = new MyGlobals(ctx);
        lvFriends = findViewById(R.id.lvFriends);
        lvFriends.setAdapter(friendsAdapter);
        //Log.d("friends", "from profile" + fl.toString());
        getFriendsList();
    }

    public List<String> getFriendsList() {
        final List<String> friendslist = new ArrayList<String>();
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                /* handle the result */
                Log.e("Friends List: 1", response.toString());
                try {
                    JSONObject responseObject = response.getJSONObject();
                    JSONArray dataArray = responseObject.getJSONArray("data");
                    Log.d("friendsprogress", dataArray.toString());

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        Log.d("friendsprogress", dataObject.toString());
                        String fbId = dataObject.getString("id");
                        String fbName = dataObject.getString("name");
                        Log.e("FbId", fbId);
                        Log.e("FbName", fbName);
                        String photourl = "http://graph.facebook.com/" + fbId + "/picture?type=square";
                        Log.d("friend", "added: " + fbName + " " +  photourl);
                        Friend lfriend = new Friend(fbId, fbName, photourl, null);
                        Log.d("friend", "added: " + fbName + " " +  photourl);
                        friendsAdapter.objects.add(lfriend);
                        Log.d("friend", "added: " + fbName + " " +  photourl);
                        friendsAdapter.notifyDataSetChanged();
                        Log.d("friend", "added: " + fbName + " " +  photourl);
                        friendslist.add(fbId);

                    }
                } catch (JSONException e) {
                    Log.e("friendsexception", e.toString());
                    e.printStackTrace();
                } finally {
                    Log.d("friendslist", "hideLoadingProgress();");
                }
            }
        }).executeAsync();
        return friendslist;
    }

    void uploadImage() {
        if (pathtoimage != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference();

            Uri file = loadedUri;//Uri.fromFile(new File(pathtoimage));

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            StorageReference imgsref = storageRef.child("users/" + user.getUid() + "/photo");
            UploadTask uploadTask = imgsref.putFile(file);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                storageRef.child("users/" + user.getUid() + "/photo").getDownloadUrl().addOnSuccessListener(uri -> {
                    // Got the download URL for 'users/me/profile.png'
                    Log.d("urlgetter", uri.toString());
                    fileUrl = uri.toString();
                    Map<String, Object> hmap = new HashMap<>();
                    hmap.put("photourl", fileUrl);
                    Log.d("puttingphoto", "url: " + fileUrl);
                    db.collection("users")
                            .document(user.getUid())
                            .update(hmap);
                    Toast.makeText(ctx, "Image Set Successfully!", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(exception -> {
                    Log.d("puttingphoto", exception.toString());
                    // Handle any errors
                });
            });

            //Log.d("urlgetter", fileUrl);
        } else {
            Log.d("puttingphoto", "nullpath");
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

        new Thread(() -> {
            if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_PICTURE) {
                    // Get the url from data
                    final Uri selectedImageUri = data.getData();
                    if (null != selectedImageUri) {
                        // Get the path from the Uri
                        String path = getPathFromURI(selectedImageUri);
                        pathtoimage = path;
                        Log.i("imageloader", "Image Path : " + path + " URI: " + selectedImageUri);
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
