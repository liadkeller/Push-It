package com.liadk.android.pushit;

import android.net.Uri;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class Page {
    UUID mId;
    String mName = "";
    String mDescription = "";
    Uri mLogoImageUri;
    ArrayList<UUID> mItemsIdentifiers;
    ArrayList<String> mFollowersIdentifiers; // users IDs

    PageSettings settings;

    enum Design {
        ALL_HEADERS, SINGLE_HEADER, NO_IMAGES;

        public static Design getDesign(String design) {
            if("All Headers".equals(design))
                return ALL_HEADERS;
            else if("No Images".equals(design))
                return NO_IMAGES;
            else
                return SINGLE_HEADER;
        }

        @Override
        public String toString() {
            if(this == ALL_HEADERS)
                return "All Headers";
            else if (this == NO_IMAGES)
                return "No Images";
            else
                return "Single Header";
        }
    }

    public class PageSettings {
        public Design design;

        public PageSettings() {
            if(design == null) {
                design = Design.SINGLE_HEADER; // Default
            }
        }
    }


    public Page() {
        mId = UUID.randomUUID();
        mItemsIdentifiers = new ArrayList<>();
        mFollowersIdentifiers = new ArrayList<>();
        settings = new PageSettings();
    }

    public Page(String name) {
        this();
        mName = name;
    }


    // only details needed for explore/follow tabs
    public static Page getPageDetailsFromDB(DataSnapshot ds) {
        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());
        page.mName = (String) ds.child("name").getValue();
        page.mDescription = (String) ds.child("description").getValue();
        return page;
    }

    // details needed for page setting screen
    public static Page getPageSettingsFromDB(DataSnapshot ds) {
        Page page = getPageDetailsFromDB(ds);
        page.settings.design = Design.getDesign((String) ds.child("settings").child("design").getValue());

        return page;
    }

    public static Page fromDB(DataSnapshot ds) {
        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());
        page.mName = (String) ds.child("name").getValue();
        page.mDescription = (String) ds.child("description").getValue();
        for(DataSnapshot dataSnapshot : ds.child("items").getChildren()) {
            UUID itemId = UUID.fromString(dataSnapshot.getKey());
            page.mItemsIdentifiers.add(itemId);
        }
        if((ArrayList<String>) ds.child("followers").getValue() != null)
            page.mFollowersIdentifiers = (ArrayList<String>) ds.child("followers").getValue();
        page.settings.design = Design.getDesign((String) ds.child("settings").child("design").getValue());

        return page;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public void setLogoImageUrl(Uri logoImageUrl) {
        this.mLogoImageUri = logoImageUrl;
    }

    public void setItemsIdentifiers(ArrayList<UUID> itemsIdentifiers) {
        this.mItemsIdentifiers = itemsIdentifiers;
    }

    public void setFollowersIdentifiers(ArrayList<String> followersIdentifiers) {
        this.mFollowersIdentifiers = followersIdentifiers;
    }

    public void addItem(UUID id) {
        mItemsIdentifiers.add(id);
    }

    public void addFollower(String uid) { mFollowersIdentifiers.add(uid); }

    public void removeFollower(String uid) { mFollowersIdentifiers.remove(uid); }


    public UUID getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public Uri getLogoImageUrl() {
        return mLogoImageUri;
    }

    public ArrayList<UUID> getItemsIdentifiers() {
        return mItemsIdentifiers;
    }

    public ArrayList<String> getFollowersIdentifiers() {
        return mFollowersIdentifiers;
    }
}
