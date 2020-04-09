package com.example.donappt5.helpclasses;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MyUser {

    public String username;
    public String email;
    public Uri photoUrl;
    public String uid;

    public MyUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public MyUser(String gusername, String gemail, Uri gphotoUrl, String guid) {
        this.username = gusername;
        this.email = gemail;
        photoUrl = gphotoUrl;
        uid = guid;
    }

}
