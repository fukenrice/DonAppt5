
package com.example.donappt5.helpclasses;

public class Charity {
    public String name; //TODO change to setters and getters
    public String briefDescription;
    public String fullDescription;
    public float trust;
    public int image;
    public int id;


    public Charity(String gname, String gbdesc, String gfdesc, float gtrust, int gim, int gid) {
        name = gname;
        briefDescription = gbdesc;
        fullDescription = gfdesc;
        trust = gtrust;
        image = gim;
        id = gid;
    }
    public Charity() {
        name = "defaultconstructor";
        briefDescription = "defaultconstructor";
        fullDescription = "defaultconstructor";
        trust = -1;
        id = -2;
    }
};