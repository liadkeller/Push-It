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

        if(followedPages == null)
            this.followedPages = new ArrayList<>();
        else
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
        followedPages = new ArrayList<>();
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

    public void setPage(String pageId) {
        this.status = true;
        this.pageId = pageId;
    }

    public void followPage(Page page) {
        this.followedPages.add(page.getId().toString());
    }

    public void unfollowPage(Page page) {
        this.followedPages.remove(page.getId().toString());
    }

    public boolean isFollowing(Page page) {
        return this.followedPages.contains(page.getId().toString());
    }

    public void setContentCreator() {
        this.status = true;
    }

    public void setContentFollower() {
        this.status = false;
    }
}