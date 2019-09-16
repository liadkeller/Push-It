package com.liadk.android.pushit;

import java.util.ArrayList;

public class PushItUser {
    private String email;
    private boolean status;
    private String pageId; // page available only if status == true
    private ArrayList<String> followedPages;

    public PushItUser(String email, boolean status, String pageId, ArrayList<String> followedPages) {
        this.email = email;
        this.status = status;
        this.followedPages = followedPages;

        if(status)
            this.pageId = pageId;
        else
            this.pageId = null;
    }

    public PushItUser(String email, boolean status, String pageId) {
        this(email, status, pageId, null);
    }

    public PushItUser() {
        status = false;
    }

    public String getEmail() {
        return email;
    }

    public boolean getStatus() {
        return status;
    }

    public String getPageId() {
        return pageId;
    }

    public ArrayList<String> getFollowedPages() {
        return followedPages;
    }

    public void addPage(String pageId) {
        status = true;
        this.pageId = pageId;
    }
}