package com.example.donappt5.helpclasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.donappt5.BrowseActivity;
import com.example.donappt5.CharitiesMapActivity;
import com.example.donappt5.CharityCreationActivity;
import com.example.donappt5.CharityListActivity;
import com.example.donappt5.ProfileActivity;
import com.example.donappt5.R;
import com.example.donappt5.SettingsActivity;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

public class MyGlobals{
    Context mContext;

    // constructor
    public MyGlobals(Context context){
        this.mContext = context;
    }

    public String getUserName(){
        return "test";
    }
    String photourlfromstore;
    public DrawerLayout drawerlayout;

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public void setupNavDrawer(Context ctx, Activity activity, DrawerLayout gdrl) {
        drawerlayout = gdrl;
        ActionBarDrawerToggle actionbartoggle = new ActionBarDrawerToggle(activity, drawerlayout, R.string.Open, R.string.Close);

        drawerlayout.addDrawerListener(actionbartoggle);
        actionbartoggle.syncState();


        NavigationView navigationview = (NavigationView) activity.findViewById(R.id.nv);
        navigationview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.account:
                        Toast.makeText(ctx, "My Account", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx, ProfileActivity.class);
                        activity.startActivity(intent);
                        break;
                    case R.id.settings:
                        Toast.makeText(ctx, "Settings", Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(ctx, SettingsActivity.class);
                        activity.startActivity(intent2);
                        break;
                    case R.id.create:
                        Toast.makeText(ctx, "Create Charity", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(ctx, CharityCreationActivity.class);
                        activity.startActivity(intent1);
                        break;
                    case R.id.map:
                        Toast.makeText(ctx, "Charities map", Toast.LENGTH_SHORT).show();
                        Intent intent3 = new Intent(ctx, CharitiesMapActivity.class);
                        activity.startActivity(intent3);
                        break;
                    case R.id.browse:
                        Toast.makeText(ctx, "Browse", Toast.LENGTH_SHORT).show();
                        Intent intent4 = new Intent(ctx, BrowseActivity.class);
                        activity.startActivity(intent4);
                        break;
                    case R.id.list:
                        Toast.makeText(ctx, "Charity List", Toast.LENGTH_SHORT).show();
                        Intent intent5 = new Intent(ctx, CharityListActivity.class);
                        activity.startActivity(intent5);
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

    public List<String> getFriendsList() {
        final List<String> friendslist = new ArrayList<String>();
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                /* handle the result */
                Log.e("Friends List: 1", response.toString());
                try {
                    JSONObject responseObject = response.getJSONObject();
                    JSONArray dataArray = responseObject.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        String fbId = dataObject.getString("id");
                        String fbName = dataObject.getString("name");
                        Log.e("FbId", fbId);
                        Log.e("FbName", fbName);
                        friendslist.add(fbId);
                        Log.d("friendslist", "fbid: " + fbId);
                    }
                    Log.e("fbfriendList", friendslist.toString());
                    List<String> list = friendslist;
                    String friends = "";
                    if (list != null && list.size() > 0) {
                        friends = list.toString();
                        if (friends.contains("[")) {
                            friends = (friends.substring(1, friends.length() - 1));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    Log.d("friendslist", "hideLoadingProgress();");
                }
            }
        }).executeAsync();
        return friendslist;
    }
}
