package com.liadk.android.pushit;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class Page {
    private UUID mId;
    private String mName = "";
    private String mDescription = "";
    private ArrayList<UUID> mItemsIdentifiers;

    private boolean mIsPrivate;
    private HashMap<String, Boolean> mFollowersIdentifiers; // users IDs - true if follow request accepted

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

    // only details needed for explore/follow tabs
    static Page getPageDetailsFromDB(DataSnapshot ds) {
        if (isNull(ds)) return null;

        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());
        page.mName = (String) ds.child("name").getValue();
        page.mDescription = (String) ds.child("description").getValue();

        if (ds.child("private").getValue() != null)
            page.mIsPrivate = (boolean) ds.child("private").getValue();
        return page;
    }


    public Page() {
        mId = UUID.randomUUID();
        mItemsIdentifiers = new ArrayList<>();
        mIsPrivate = false;
        mFollowersIdentifiers = new HashMap<>();
        settings = new PageSettings();
    }

    public Page(String name) {
        this();
        mName = name;
    }

    private static boolean isNull(DataSnapshot ds) {
        return ds.getValue() == null || (ds.child("deleted").getValue() != null && (boolean) ds.child("deleted").getValue());
    }

    // details needed for page setting screen
    static Page getPageSettingsFromDB(DataSnapshot ds) {
        if(isNull(ds)) return null;

        Page page = getPageDetailsFromDB(ds);
        page.settings.design = Design.getDesign((String) ds.child("settings").child("design").getValue());

        return page;
    }

    static Page getPageFollowersFromDB(DataSnapshot ds) {
        if(isNull(ds)) return null;

        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());

        if(ds.child("private").getValue() != null)
            page.mIsPrivate = (boolean) ds.child("private").getValue();

        if (ds.child("followers").getValue() != null)
            page.mFollowersIdentifiers = (HashMap<String, Boolean>) ds.child("followers").getValue();

        return page;
    }

    static Page fromDB(DataSnapshot ds) {
        if(isNull(ds)) return null;

        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());
        page.mName = (String) ds.child("name").getValue();
        page.mDescription = (String) ds.child("description").getValue();
        for(DataSnapshot dataSnapshot : ds.child("items").getChildren()) {
            UUID itemId = UUID.fromString(dataSnapshot.getKey());
            page.mItemsIdentifiers.add(itemId);
        }
        if(ds.child("private").getValue() != null)
            page.mIsPrivate = (boolean) ds.child("private").getValue();
        if (ds.child("followers").getValue() != null)
            page.mFollowersIdentifiers = (HashMap<String, Boolean>) ds.child("followers").getValue();
        page.settings.design = Design.getDesign((String) ds.child("settings").child("design").getValue());

        return page;
    }

    // if page public, adds follower. else adds to following requests list
    void addNewFollower(PushItUser user, String userId) {
        mFollowersIdentifiers.put(userId, this.isPublic());

        if(mFollowersIdentifiers.get(userId))
            user.followPage(this);
    }

    public void setName(String name) {
        this.mName = name;
    }

    // approves follower
    void approveFollower(PushItUser user, String userId) {
        mFollowersIdentifiers.put(userId, true);
        user.followPage(this);
    }

    public void addItem(UUID id) {
        mItemsIdentifiers.add(id);
    }

    void removeFollower(PushItUser user, String userId) {
        user.unfollowPage(this);
        mFollowersIdentifiers.remove(userId);
    }

    // returns true if the user has requested to follow this page
    boolean hasFollowedBy(String userId) {
        return mFollowersIdentifiers.containsKey(userId);
    }

    String getDescription() {
        return mDescription;
    }

    void setDescription(String description) {
        this.mDescription = description;
    }

    ArrayList<UUID> getItemsIdentifiers() {
        return mItemsIdentifiers;
    }

    public void setPrivate(boolean isPrivate) {
        mIsPrivate = isPrivate;
    }


    public UUID getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setItemsIdentifiers(ArrayList<UUID> itemsIdentifiers) {
        this.mItemsIdentifiers = itemsIdentifiers;
    }

    HashMap<String, Boolean> getFollowersIdentifiers() {
        return mFollowersIdentifiers;
    }

    public boolean isPrivate() {
        return mIsPrivate;
    }

    public boolean isPublic() {
        return !mIsPrivate;
    }

    public class PageSettings {
        Design design;

        PageSettings() {
            if (design == null) {
                design = Design.SINGLE_HEADER; // Default
            }
        }
    }
}
