package com.liadk.android.pushit;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    private static DatabaseManager sDatabaseManager;
    private Context mAppContext;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mItemsDatabase;
    private DatabaseReference mPagesDatabase;
    private DatabaseReference mUsersDatabase;


    public DatabaseManager(Context appContext) {
        mAppContext = appContext;

        mDatabase = FirebaseDatabase.getInstance();
        mItemsDatabase = mDatabase.getReference("items");
        mPagesDatabase = mDatabase.getReference("pages");
        mUsersDatabase = mDatabase.getReference("users");
    }

    public static DatabaseManager get(Context c) {
        if(sDatabaseManager == null) {
            sDatabaseManager = new DatabaseManager(c.getApplicationContext());
        }

        return sDatabaseManager;
    }


    public ValueEventListener addItemsListener(ValueEventListener listener) {
        return mItemsDatabase.addValueEventListener(listener);
    }

    public ValueEventListener addPagesListener(ValueEventListener listener) {
        return mPagesDatabase.addValueEventListener(listener);
    }

    public void addUsersSingleEventListener(ValueEventListener listener) {
        mUsersDatabase.addListenerForSingleValueEvent(listener);
    }

    public ValueEventListener addDatabaseListener(ValueEventListener listener) {
        return mDatabase.getReference().addValueEventListener(listener);
    }

    public void removePagesListener(ValueEventListener listener) {
        mPagesDatabase.removeEventListener(listener);
    }

    public void removeItemsListener(ValueEventListener listener) {
        mItemsDatabase.removeEventListener(listener);
    }

    public void removeDatabaseListener(ValueEventListener listener) {
        mDatabase.getReference().removeEventListener(listener);
    }


    public void pushItemToDB(Item item) {
        mItemsDatabase.child(item.getId().toString()).child("title").setValue(item.getTitle());
        mItemsDatabase.child(item.getId().toString()).child("author").setValue(item.getAuthor());
        mItemsDatabase.child(item.getId().toString()).child("has-image").setValue(false); // triggers refreshing images after upload to storage
        mItemsDatabase.child(item.getId().toString()).child("time").setValue(item.getTimeLong());
        mItemsDatabase.child(item.getId().toString()).child("original-time").setValue(item.getOriginalTimeLong());
        mItemsDatabase.child(item.getId().toString()).child("owner").setValue(item.getOwnerId().toString());
        mItemsDatabase.child(item.getId().toString()).child("state").setValue(item.getState().toString());

        mItemsDatabase.child(item.getId().toString()).child("counter").setValue(item.getSegmentsCounter());
        for(int i = 0; i <= item.getSegmentsCounter(); i++)
            mItemsDatabase.child(item.getId().toString()).child("text").child(i+"").setValue(item.getTextSegments().get(i));
    }

    public void pushPageToDB(Page page) {
        mPagesDatabase.child(page.getId().toString()).child("name").setValue(page.getName());
        mPagesDatabase.child(page.getId().toString()).child("description").setValue(page.getDescription());
        for(UUID itemId : page.getItemsIdentifiers())
            mPagesDatabase.child(page.getId().toString()).child("items").child(itemId.toString()).setValue(true);
        mPagesDatabase.child(page.getId().toString()).child("private").setValue(page.isPrivate());
        mPagesDatabase.child(page.getId().toString()).child("followers").setValue(page.getFollowersIdentifiers());
        mPagesDatabase.child(page.getId().toString()).child("settings").child("design").setValue(page.settings.design.toString());
    }

    public void addItemToPage(Item item) { // doesn't push item to db, but adds item to the page's items list
        DatabaseReference pageItemsDatabase = mPagesDatabase.child(item.getOwnerId().toString()).child("items");
        pageItemsDatabase.child(item.getId().toString()).setValue(true);
    }

    public void pushUserToDB(PushItUser user, String userId) {
        pushUserToDB(user, userId, null);
    }

    public void pushUserToDB(PushItUser user, String userId, OnCompleteListener listener) {
        mUsersDatabase.child(userId).setValue(user).addOnCompleteListener(listener);
    }

    // updates page followers list and user followed list in DB
    public boolean followPage(PushItUser user, String userId, Page page) {
        if(user == null || userId == null) return false;

        updatePageFollowers(page);
        mUsersDatabase.child(userId).child("followedPages").setValue(user.getFollowedPages());
        return true;
    }

    public void updatePageFollowers(Page page) {
        mPagesDatabase.child(page.getId().toString()).child("followers").setValue(page.getFollowersIdentifiers());
    }

    public void refreshItemImage(Item item) {
        mItemsDatabase.child(item.getId().toString()).child("has-image").setValue(true);  // triggers refreshing image
    }

    public void updateItemState(Item item) {
        mItemsDatabase.child(item.getId().toString()).child("state").setValue(item.getState().toString());
    }


    public void deleteItem(Item item) {
        DatabaseReference pageItemsDatabase = mPagesDatabase.child(item.getOwnerId().toString()).child("items");

        mItemsDatabase.child(item.getId().toString()).removeValue();
        pageItemsDatabase.child(item.getId().toString()).removeValue();
    }

    public void deleteUser(PushItUser user, String userId) {
        // Currently Deletes the user from the db
        // Does not delete any page linked to it
        // If page deletion is enabled, the page entry will be deleted but the items entries will be marked as deleted and will be kept

        mUsersDatabase.child(userId).removeValue();
        // if(user.getStatus()) deletePage(user.getPageId());                      TODO Tremendously DANGEROUS!
    }

    public void deletePage(final String pageId) {
        // Currently deletes the page entry but doesn't delete the items data (title, article text, images)
        // This means the page can be easily recovered, but that the data keeps using the cloud space (might wanna delete the items data 30 days after user deletion)
        // If a decision to commit hard delete of the items data is made, including all text and media files, you should consider very cautiously whether to call this function (i. e. in deleteUser())
        // You should also consider whether the items media should be deleted from the storage

        if(pageId == null) return;

        mPagesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Page page = Page.fromDB(dataSnapshot.child("pages").child(pageId));

                mPagesDatabase.child(pageId).removeValue();                                         // delete page

                for(UUID itemId : page.getItemsIdentifiers())                                       // delete page items
                    mItemsDatabase.child(itemId.toString()).child("owner-deleted").setValue(true);  // instead of deleting the items for good, keep them for temp period
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }


    public void setPageName(Page page) {
        mPagesDatabase.child(page.getId().toString()).child("name").setValue(page.getName());
    }

    public void setPageDescription(Page page) {
        mPagesDatabase.child(page.getId().toString()).child("description").setValue(page.getDescription());
    }

    public void setPagePrivacy(Page page) {
        mPagesDatabase.child(page.getId().toString()).child("private").setValue(page.isPrivate());
    }

    public void setPageDesign(Page page) {
        mPagesDatabase.child(page.getId().toString()).child("settings").child("design").setValue(page.settings.design.toString());
    }
}
