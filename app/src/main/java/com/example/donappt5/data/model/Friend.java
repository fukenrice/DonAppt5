package com.example.donappt5.data.model;

import java.util.List;

public class Friend {
    public String id;
    public String photourl;
    public String name;
    public List<String> chars;

    public Friend(String gid, String gname, String gphotourl, List<String> gchars) {
        name = gname;
        id = gid;
        photourl = gphotourl;
        chars = gchars;
    }
    public Friend() {
        name = "defaultconstructor";
        id = "defaultconstructor";
        photourl = "defaultconstructor";

    }
}
