package com.liadk.android.pushit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private static final String KEY_LATEST_UPDATE = "latestUpdate";
    static final String MAIN_CHANNEL_ID = "notificationChannelId";

    private FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseManager = DatabaseManager.get(this);

        mDatabaseManager.addNotificationsChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final PushNotification notification = dataSnapshot.getValue(PushNotification.class);
                final long latestUpdateTime = PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getLong(KEY_LATEST_UPDATE, 0);

                Log.d(TAG, "[" + notification.getPageName() + "] <" + notification.getTitle() + "> " + "publish time: " + notification.getPublishTime() + " id: " + notification.getId());
                Log.d(TAG, "latest update time: " + latestUpdateTime);
                Log.d(TAG, "diff: " + (notification.getPublishTime() - latestUpdateTime));

                if(notification.getPublishTime() <= latestUpdateTime) return;

                mDatabaseManager.addDatabaseSingleEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mAuth.getCurrentUser() == null) return;

                        String userId = mAuth.getCurrentUser().getUid();
                        PushItUser user = dataSnapshot.child("users").child(userId).getValue(PushItUser.class);

                        Page page = Page.fromDB(dataSnapshot.child("pages").child(notification.getPageId().toString()));

                        if(user != null && notification != null && page != null && user.isFollowing(page)) {
                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel(MAIN_CHANNEL_ID, "Push Notification", NotificationManager.IMPORTANCE_HIGH);
                                notificationManager.createNotificationChannel(channel);
                            }

                            notificationManager.notify(notification.getId(), notification.getNotification(NotificationService.this));
                            Log.d(TAG, "NOTIFYING [" + notification.getPageName() + "] <" + notification.getTitle() + "> diff: " + (notification.getPublishTime() - latestUpdateTime));

                            PreferenceManager.getDefaultSharedPreferences(NotificationService.this)
                                    .edit()
                                    .putLong(KEY_LATEST_UPDATE, notification.getPublishTime())
                                    .commit();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
