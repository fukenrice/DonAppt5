package com.example.donappt5.data.model;

public class ForumMessage {
    public String messageid;
    public String userid;
    public String username;
    public String profileurl;
    public String comment;
    public String datetime;

    public ForumMessage(String gmid, String guid, String gname, String gprof, String gcomm, String gdate) {
        messageid = gmid;
        userid = guid;
        username = gname;
        profileurl = gprof;
        comment = gcomm;
        datetime = gdate;
    }
    public ForumMessage() {
        messageid = "defaultconstructor";
        userid = "defaultconstructor";
        username = "defaultconstructor";
        profileurl = "defaultconstructor";
        comment = "defaultconstructor";
        datetime = "defaultconstructor";
    }
}
