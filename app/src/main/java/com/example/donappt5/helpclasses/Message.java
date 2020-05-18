package com.example.donappt5.helpclasses;

public class Message {
    public String messageid;
    public String userid;
    public String username;
    public String profileurl;
    public String comment;
    public String datetime;

    public Message(String gmid, String guid, String gname, String gprof, String gcomm, String gdate) {
        messageid = gmid;
        userid = guid;
        username = gname;
        profileurl = gprof;
        comment = gcomm;
        datetime = gdate;
    }
    public Message() {
        messageid = "defaultconstructor";
        userid = "defaultconstructor";
        username = "defaultconstructor";
        profileurl = "defaultconstructor";
        comment = "defaultconstructor";
        datetime = "defaultconstructor";
    }
}
