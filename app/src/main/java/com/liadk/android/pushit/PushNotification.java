package com.liadk.android.pushit;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

class PushNotification {

    private String itemId;
    private String title;
    private Uri imageUri;
    private long publishTime;

    private String pageId;
    private String pageName;


    public PushNotification() {}

    public PushNotification(String itemId, String pageId, long publishTime, String title, String pageName) {
        this(itemId, title, null, publishTime, pageId, pageName);
    }


    public PushNotification(String itemId, String pageId, Uri imageUri, long publishTime, String title, String pageName) {
        this.itemId = itemId;
        this.title = title;
        this.imageUri = imageUri;
        this.publishTime = publishTime;
        this.pageId = pageId;
        this.pageName = pageName;
    }

    public PushNotification(Item item, Page owner, String alternativeTitle, boolean same) {
        itemId = item.getId().toString();
        title = (same) ? item.getTitle() : alternativeTitle;
        imageUri = item.getImageUri();
        publishTime = item.getPublishTimeLong();

        pageId = owner.getId().toString();
        pageName = owner.getName();
    }

    int getId() {
        return itemId.hashCode();
    }


    Notification getNotification(final Context context) {
        final RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.remoteview_notification);
        notificationLayout.setTextViewText(R.id.remoteview_notification_headline, pageName);
        notificationLayout.setTextViewText(R.id.remoteview_notification_short_message, title);

        Intent itemIntent = new Intent(context.getApplicationContext(), ItemActivity.class);
        itemIntent.putExtra(ItemFragment.EXTRA_ID, UUID.fromString(itemId));
        itemIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, getId(), itemIntent, 0);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.pushit_transperent_logo)
                .setContentTitle(pageName)
                .setContentText(title)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
            notificationBuilder.setCustomBigContentView(notificationLayout);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            notificationBuilder.setGroup(pageId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(context.getResources().getColor(R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(NotificationService.MAIN_CHANNEL_ID);
        }

        final Notification notification = notificationBuilder.build();

        final NotificationTarget notificationTarget = new NotificationTarget(context, R.id.remoteview_notification_image, notificationLayout, notification, getId());

        FirebaseStorage.getInstance().getReference("items").child(itemId).child("notification-image.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.getException() == null) {
                    if(notificationLayout != null)
                        Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(task.getResult())
                            .into(notificationTarget);
                        notificationLayout.setViewVisibility(R.id.remoteview_notification_image, View.VISIBLE);
                }

                else {
                    FirebaseStorage.getInstance().getReference("items").child(itemId).child("image.png").getDownloadUrl().addOnCompleteListener(this);
                }
            }
        });

        return notification;
    }

    int getGroupId() {
        return pageId.hashCode();
    }

    Notification getSummaryNotification(Context context) {
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setContentTitle(pageName)
                .setSmallIcon(R.drawable.pushit_transperent_logo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            notificationBuilder.setGroup(pageId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(context.getResources().getColor(R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(NotificationService.MAIN_CHANNEL_ID);
        }

        return notificationBuilder.build();
    }

    public String getPageId() {
        return pageId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public String getPageName() {
        return pageName;
    }
}