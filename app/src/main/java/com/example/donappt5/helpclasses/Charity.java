
package com.example.donappt5.helpclasses;

public class Charity {
    public int id;
    public String name; //TODO change to setters and getters
    public String briefDescription;
    public String fullDescription;
    public float trust;
    public int image;
    public String photourl;
    public String paymentUrl;


    public Charity(String gname, String gbdesc, String gfdesc, float gtrust, int gim, int gid, String gphotourl) {
        this.name = gname;
        this.briefDescription = gbdesc;
        this.fullDescription = gfdesc;
        this.trust = gtrust;
        this.image = gim;
        this.id = gid;
        this.photourl = gphotourl;
        this.paymentUrl = null;
    }

    public Charity(String gname, String gbdesc, String gfdesc, float gtrust, int gim, int gid, String gphotourl, String paymentUrl) {
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
};