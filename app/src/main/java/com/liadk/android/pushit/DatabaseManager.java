package com.liadk.android.pushit;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ChildEventListener;
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
    private DatabaseReference mNotificationsDatabase;



    public DatabaseManager(Context appContext) {
        mAppContext = appContext;

        mDatabase = FirebaseDatabase.getInstance();
        mItemsDatabase = mDatabase.getReference("items");
        mPagesDatabase = mDatabase.getReference("pages");
        mUsersDatabase = mDatabase.getReference("users");
        mNotificationsDatabase = mDatabase.getReference("notifications");
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

    public void addNotificationsChildListener(ChildEventListener listener) {
        mNotificationsDatabase.addChildEventListener(listener);
    }

    public ValueEventListener addDatabaseListener(ValueEventListener listener) {
        return mDatabase.getReference().addValueEventListener(listener);
    }

    public void addDatabaseSingleEventListener(ValueEventListener listener) {
        mDatabase.getReference().addListenerForSingleValueEvent(listener);
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
        mItemsDatabase.child(item.getId().toString()).child("publish-time").setValue(item.getPublishTimeLong());
        mItemsDatabase.child(item.getId().toString()).child("owner").setValue(item.getOwnerId().toString());
        mItemsDatabase.child(item.getId().toString()).child("state").setValue(item.getState().toString());

        mItemsDatabase.child(item.getId().toString()).child("counter").setValue(item.getSegmentsCounter());
        for(int i = 0; i <= item.getSegmentsCounter(); i++)
            mItemsDatabase.child(item.getId().toString()).child("text").child(i+"").setValue(item.getTextSegments().get(i));
    }

    public void addItemToPage(Item item) { // doesn't push item to db, but adds item to the page's items list
        DatabaseReference pageItemsDatabase = mPagesDatabase.child(item.getOwnerId().toString()).child("items");
        pageItemsDatabase.child(item.getId().toString()).setValue(true);
    }

    public void pushPageToDB(Page page) {
        mPagesDatabase.child(page.getId().toString()).child("name").setValue(page.getName());
        mPagesDatabase.child(page.getId().toString()).child("description").setValue(page.getDescription());
        for(UUID itemId : page.getItemsIdentifiers())
            mPagesDatabase.child(page.getId().toString()).child("items").child(itemId.toString()).setValue(true);
        mPagesDatabase.child(page.getId().toString()).child("private").setValue(page.isPrivate());
        mPagesDatabase.child(page.getId().toString()).child("followers").setValue(page.getFollowersIdentifiers());
        mPagesDatabase.child(page.getId().toString()).child("settings").child("design").setValue(page.settings.design.toString());
        mPagesDatabase.child(page.getId().toString()).child("deleted").setValue(false);
    }

    public void pushUserToDB(PushItUser user, String userId) {
        pushUserToDB(user, userId, null);
    }

    public void pushUserToDB(PushItUser user, String userId, OnCompleteListener listener) {
        mUsersDatabase.child(userId).setValue(user).addOnCompleteListener(listener);
    }

    public void pushNotificationToDB(PushNotification notification) {
        mNotificationsDatabase.child(notification.getId() + "").setValue(notification);
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

    public void updateItemPublished(Item item) {
        mItemsDatabase.child(item.getId().toString()).child("state").setValue(item.getState().toString());
        mItemsDatabase.child(item.getId().toString()).child("time").setValue(item.getTimeLong());
        mItemsDatabase.child(item.getId().toString()).child("publish-time").setValue(item.getPublishTimeLong());
    }


    public void deleteItem(Item item) {
        DatabaseReference pageItemsDatabase = mPagesDatabase.child(item.getOwnerId().toString()).child("items");

        mItemsDatabase.child(item.getId().toString()).removeValue();
        pageItemsDatabase.child(item.getId().toString()).removeValue();
    }

    // Deletes the user from db and soft-deletes the user page (if exists)
    public void deleteUser(PushItUser user, String userId) {
        mUsersDatabase.child(userId).removeValue();

        if(user.getStatus())
            deletePage(user.getPageId());
    }

    public void deletePage(String pageId) {
        deletePage(pageId, true);
    }

    public void recoverPage(String pageId) {
        deletePage(pageId, false);
    }

    // true for deleting, false for recovering
    // Currently marks the page as deleted but does not delete the page entry nor the items'.
    // If you want to hard delete the page and the items entries, you may wanna consider delete them from the storage as well
    private void deletePage(final String pageId, final boolean delete) {
        if(pageId == null) return;

        mPagesDatabase.child(pageId).child("deleted").setValue(delete);                       // mark the page as deleted

        mPagesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                for(DataSnapshot itemDataSnapshot : ds.child(pageId).child("items").getChildren()) {
                    String itemId = itemDataSnapshot.getKey();
                    mItemsDatabase.child(itemId).child("owner-deleted").setValue(delete);  // mark the page items as deleted
                }
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


    public void setUserStatus(PushItUser user, String userId, OnCompleteListener listener) {
        mUsersDatabase.child(userId).child("status").setValue(user.getStatus()).addOnCompleteListener(listener);
    }

    public void setUserEmail(PushItUser user, String userId, OnCompleteListener listener) {
        mUsersDatabase.child(userId).child("email").setValue(user.getEmail()).addOnCompleteListener(listener);
    }
}
