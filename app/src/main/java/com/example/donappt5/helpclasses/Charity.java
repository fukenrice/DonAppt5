
package com.example.donappt5.helpclasses;

import com.example.donappt5.R;
import com.google.firebase.firestore.DocumentSnapshot;

public class Charity {
    public int id;
    public String name; //TODO change to setters and getters
    public String firestoreID;
    public String briefDescription;
    public String fullDescription;
    public float trust;
    public int image;
    public String photourl;
    public String paymentUrl;


    public Charity(String gfirestoreID, String gname, String gbdesc, String gfdesc, float gtrust, int gim, int gid, String gphotourl) {
        this.firestoreID = gfirestoreID;
        this.name = gname;
        this.briefDescription = gbdesc;
        this.fullDescription = gfdesc;
        this.trust = gtrust;
        this.image = gim;
        this.id = gid;
        this.photourl = gphotourl;
        this.paymentUrl = null;
    }

    public Charity(String gfirestoreID, String gname, String gbdesc, String gfdesc, float gtrust, int gim, int gid, String gphotourl, String paymentUrl) {
        this.firestoreID = gfirestoreID;
        this.name = gname;
        this.briefDescription = gbdesc;
        this.fullDescription = gfdesc;
        this.trust = gtrust;
        this.image = gim;
        this.id = gid;
        this.photourl = gphotourl;
        this.paymentUrl = paymentUrl;
    }

    public Charity() {
        name = "defaultconstructor";
        briefDescription = "defaultconstructor";
        fullDescription = "defaultconstructor";
        trust = -1;
        id = -2;
    }

    public Charity(DocumentSnapshot document) {
        firestoreID = document.getId();
        name = document.getString("name");
        fullDescription = document.getString("description");
        if (fullDescription == null) {
            briefDescription = null;
        } else {
            briefDescription = fullDescription.substring(0, Math.min(fullDescription.length(), 50));
        }
        photourl = document.getString("photourl");
        paymentUrl = document.getString("qiwiurl");
        image = R.drawable.ic_launcher_foreground;
        trust = -1;
        id = -2;
    }
};