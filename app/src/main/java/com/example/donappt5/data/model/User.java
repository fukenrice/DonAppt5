package com.example.donappt5.data.model;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

public class User {
    public String username;
    public String email;
    public Uri photoUrl;
    public String uid;

    public User(String gusername, String gemail, Uri gphotoUrl, String guid) {
        this.username = gusername;
        this.email = gemail;
        photoUrl = gphotoUrl;
        uid = guid;
    }
}
