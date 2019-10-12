package com.liadk.android.pushit;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class StorageManager {

    private static final String TAG = "StorageManager";

    private static StorageManager sStorageManager;
    private Context mAppContext;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mItemsStorage;
    private StorageReference mPagesStorage;

    public StorageManager(Context appContext) {
        mAppContext = appContext;

        mFirebaseStorage = FirebaseStorage.getInstance();
        mItemsStorage = mFirebaseStorage.getReference("items");
        mPagesStorage = mFirebaseStorage.getReference("pages");
    }

    public static StorageManager get(Context c) {
        if(sStorageManager == null) {
            sStorageManager = new StorageManager(c.getApplicationContext());
        }

        return sStorageManager;
    }


    public void uploadItemImages(Item item, OnCompleteListener<UploadTask.TaskSnapshot> onCompleteListener) {
        if(item.getImageUri() == null) return;
        StorageReference storageRef = mItemsStorage.child(item.getId().toString());

        uploadImage(getBytesFromUri(item.getImageUri()), storageRef.child("image.png"), onCompleteListener);

        // upload all media segments
        for(int i = 0; i < item.getSegmentsCounter(); i++) {
            String imageName = "image" + i + ".png";
            uploadImage(getBytesFromUri(item.getMediaSegments().get(i)), storageRef.child(imageName), onCompleteListener);
        }

        // remove all media segments which aren't currently registered (usually because deleted)
        for(int i = item.getSegmentsCounter(); i < EditItemFragment.MAX_MEDIA; i++) {
            String imageName = "image" + i + ".png";
            storageRef.child(imageName).delete();
        }
    }



    public void uploadNotificationImage(Item item) {
        uploadNotificationImage(item, item.getImageUri());
    }

    public void uploadNotificationImage(Item item, Uri imageUri) {
        if(imageUri == null) return;
        uploadNotificationImage(item.getId(), getBytesFromUri(imageUri));
    }

    public void uploadNotificationImage(UUID itemId, byte[] imageBytes) {
        StorageReference imageStorageRef = mItemsStorage.child(itemId.toString()).child("notification-image.png");
        uploadImage(imageBytes, imageStorageRef);
    }



    public void uploadPageLogoImage(UUID pageId, Uri imageUri) {
        if(imageUri == null) return;
        uploadPageLogoImage(pageId, getBytesFromUri(imageUri));
    }

    public void uploadPageLogoImage(UUID pageId, byte[] imageBytes) {
        StorageReference imageStorageRef = mPagesStorage.child(pageId.toString()).child("logo.png");
        uploadImage(imageBytes, imageStorageRef);
    }


    public void uploadImage(byte[] imageBytes, StorageReference storageRef) {
        if(imageBytes != null) {
            UploadTask uploadTask = storageRef.putBytes(imageBytes);
        }
    }

    public void uploadImage(byte[] imageBytes, StorageReference storageRef, OnCompleteListener<UploadTask.TaskSnapshot> onCompleteListener) {
        if(imageBytes != null) {
            UploadTask uploadTask = (UploadTask) storageRef.putBytes(imageBytes).addOnCompleteListener(onCompleteListener);
        }
    }



    public void deleteItem(Item item) {
        StorageReference itemStorageRef = mItemsStorage.child(item.getId().toString());

        // Deleting the content of the folder is equivalent to deleting the folder
        itemStorageRef.child("image.png").delete();
        itemStorageRef.child("notification-image.png").delete();
    }

    public void deletePage(Page page) {
        StorageReference pageStorageRef = mPagesStorage.child(page.getId().toString());

        // Deleting the content of the folder is equivalent to deleting the folder
        pageStorageRef.child("logo.png").delete();
    }



    public void deleteItemIImage(Item item) {
        deleteItemIImage(item, null);
    }

    public void deleteItemIImage(Item item, OnCompleteListener<Void> onCompleteListener) {
        StorageReference itemStorageRef = mItemsStorage.child(item.getId().toString());
        itemStorageRef.child("image.png").delete().addOnCompleteListener(onCompleteListener);
    }

    public void deleteItemNotificationIImage(Item item) {
        StorageReference itemStorageRef = mItemsStorage.child(item.getId().toString());
        itemStorageRef.child("notification-image.png").delete();
    }



    private byte[] getBytesFromUri(Uri imageUri) {
        byte[] imageBytes = null;

        try {
            InputStream iStream = mAppContext.getContentResolver().openInputStream(imageUri);
            imageBytes = getBytesFromStream(iStream);
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return imageBytes;
    }

    private static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    // image processing - bytes
    public static byte[] getImageBytes(Bitmap image) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return byteStream.toByteArray();
    }
}
