package com.liadk.android.pushit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Base64;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class Item {

    protected enum State {
        NEW, DRAFT, CREATED, PUBLISHED;

        public static State getState(String state) {
            if(state == null) return null;
            switch (state.toUpperCase()) {
                case "DRAFT":
                    return DRAFT;
                case "CREATED":
                    return CREATED;
                case "PUBLISHED":
                    return PUBLISHED;
                default:
                    return NEW;
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case DRAFT:
                    return "DRAFT";
                case CREATED:
                    return "CREATED";
                case PUBLISHED:
                    return "PUBLISHED";
                default:
                    return "NEW";
            }
        }
    }

    UUID mId;

    String mTitle = "";
    String mAuthor = "";
    Bitmap mImage; // TODO TAKE CARE OF new Bitmap() TODO add to Database
    Date mOriginalTime; // original creation time
    Date mTime;                  // creation time
    UUID mOwnerId;

    ArrayList<String> mTextSegments;
    ArrayList<Object> mMediaSegments;             // TODO add to Database

    int mCounter;

    State mState;


    public Item(UUID ownerId) {
        mId = UUID.randomUUID();
        mOwnerId = ownerId;

        mOriginalTime = new Date();
        mTime = mOriginalTime;

        mState = State.NEW;

        mCounter = 0;
        mTextSegments = new ArrayList<>();
        mMediaSegments = new ArrayList<>();

        mTextSegments.add("");
    }

    private Item() {
        this((UUID) null);
    }

    public Item(Item item) {
        mId = UUID.randomUUID();

        mTitle = item.mTitle;
        mImage = item.mImage;
        mAuthor = item.mAuthor;
        mOriginalTime = item.mOriginalTime;
        mTime = item.mTime;
        mOwnerId = item.mOwnerId;
        mTextSegments = new ArrayList<>(item.mTextSegments);
        mMediaSegments = new ArrayList<>(item.mMediaSegments);
        //mImages = item.mImages;
        //mVideos = item.mVideos;
        mCounter = item.mCounter;

        mState = item.mState;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setImage(Bitmap image) {
        mImage = image;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public void setTime(Date time) {
        mTime = time;
    }

    public void setCurrentTime() {
        mTime = new Date();
    }

    public void setText(String text) {
        mTextSegments.set(0, text);
    }

    public void setState(State state) {
        mState = state;
    }

    public void addText(String text) {
        mTextSegments.add(text);
    }

    public void addImage(Bitmap image) {
        mMediaSegments.add(image);
        mTextSegments.add("");
        mCounter++;
    }

    public void addVideo(MediaStore.Video video) {
        mMediaSegments.add(video);
        mTextSegments.add("");
        mCounter++;
    }

    public void setCounter(int counter) {
        mCounter = counter;
    }

    public void addToCounter() {
        mCounter++;
    }

    public void resetSegments() {
        if (mTextSegments.size() > 1) {
            mTextSegments.removeAll(mTextSegments.subList(1, mTextSegments.size()));
        }
        mMediaSegments = new ArrayList<>();
        mCounter = 0;
    }

    public void removeTextSegment(int index) {
        if (index > 0 && index <= mCounter) {
            String linebreak = (mTextSegments.get(index - 1).equals("") || mTextSegments.get(index).equals("")) ? "" : "\n";
            String text = mTextSegments.get(index - 1) + linebreak + mTextSegments.get(index);
            mTextSegments.remove(index);
            mTextSegments.set(index - 1, text);
            mCounter--;
        }
    }


    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public String getText() {
        return mTextSegments.get(0);
    }

    protected Date getOriginalTime() {
        return mOriginalTime;
    }

    public String getFormattedOriginalTime() {
        if(mOriginalTime == null) return null;
        DateFormat df = new DateFormat();
        return df.format("MMM d, hh:mm", mOriginalTime).toString();
    }

    public String getTime() {
        if(mTime == null) return null;
        DateFormat df = new DateFormat();
        return df.format("MMM d, hh:mm", mTime).toString();
    }

    public String getShortTime() {
        if(mTime == null) return null;
        DateFormat df = new DateFormat();
        return df.format("hh:mm", mTime).toString();
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getDetails() {
        if (mAuthor == null || mAuthor.equals("")) return getTime();
        else return getTime() + " | " + mAuthor;
    }

    public State getState() {
        return mState;
    }

    public UUID getOwnerId() {
        return mOwnerId;
    }

    public ArrayList<String> getTextSegments() {
        return mTextSegments;
    }

    public ArrayList<Object> getMediaSegments() {
        return mMediaSegments;
    }

    public int getSegmentsCounter() {
        return mCounter;
    }

    public long getOriginalTimeLong() {
        return mOriginalTime.getTime();
    }

    public long getTimeLong() {
        return mTime.getTime();
    }


    public void updateOnPost() {
        // TODO:
        //  Update Creation Time and all stuff when posting/publishing a NEW/DRAFT Item
    }


    public static Item fromDB(DataSnapshot ds) {
        Item item = new Item();

        item.mId = UUID.fromString(ds.getKey());
        item.mTitle = (String) ds.child("title").getValue();
        item.mAuthor = (String) ds.child("author").getValue();
        //item.mImage = bitmapFromBlob((String) ds.child("image").getValue());

        if(ds.child("owner").getValue() != null)
            item.mOwnerId = UUID.fromString((String) ds.child("owner").getValue());
        item.mState = State.getState((String) ds.child("state").getValue());

        item.mCounter = 0;
        item.mTextSegments = new ArrayList<>();
        item.mMediaSegments = new ArrayList<>();
        item.mTime = null;
        item.mOriginalTime = null;

        if(ds.child("counter").getValue() != null)
            item.mCounter = (int) ds.child("counter").getValue(Integer.class);
        for (int i = 0; i <= item.mCounter; i++)
            //if(ds.child("text").child(i + "").getValue() != null) Todo decide about adding mTextSegments list including nulls or not
            item.mTextSegments.add((String) ds.child("text").child(i + "").getValue());

        if(ds.child("time").getValue() != null)
            item.mTime = new Date( (long) ds.child("time").getValue());
        if(ds.child("original-time").getValue() != null)
            item.mOriginalTime = new Date((long) ds.child("original-time").getValue());

        downloadMedia(item);

        return item;
    }


    public void pushToDB(DatabaseReference db) {
        db.child(getId().toString()).child("title").setValue(getTitle());
        db.child(getId().toString()).child("author").setValue(getAuthor());
        //db.child(getId().toString()).child("image").setValue(getBlob(getImage())); TODO Add image to DB
        db.child(getId().toString()).child("time").setValue(getTimeLong());
        db.child(getId().toString()).child("original-time").setValue(getOriginalTimeLong());
        db.child(getId().toString()).child("owner").setValue(getOwnerId().toString());
        db.child(getId().toString()).child("state").setValue(getState().toString());

        db.child(getId().toString()).child("counter").setValue(getSegmentsCounter());
        for(int i = 0; i <= getSegmentsCounter(); i++)
            db.child(getId().toString()).child("text").child(i+"").setValue(getTextSegments().get(i));
        /* for(int i = 0; i < getSegmentsCounter(); i++)
            db.child(getId().toString()).child("media").child(i+"").setValue(getTextSegments().get(i));  TODO Add Media to DB */

        uploadMedia();
    }

    private void uploadMedia() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("items").child(getId().toString());
        //UploadTask uploadTask = storageRef.child("image.png").putBytes(getImageBytes(mImage)); // TODO Tremendously Pricey
    }

    private static void downloadMedia(final Item item) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("items").child(item.getId().toString());

        final long MAX_SIZE = 100 * 1024;

        /*storageRef.child("image.png").getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() { // TODO Tremendously Pricey
           @Override
           public void onSuccess(byte[] bytes) {
               item.mImage = bitmapFromBytes(bytes); // TODO Check IF NOT CONTRADICTS FINAL DECLARATION
           }
        });
        */
    }



    // image processing - blob
    public String getBlob(Bitmap image) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        String imageB64 = Base64.encodeToString(byteStream.toByteArray(), Base64.DEFAULT);

        return imageB64;
    }

    public static Bitmap bitmapFromBlob(String blob)
    {
        byte[] bytes = Base64.decode(blob, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // image processing - bytes
    private byte[] getImageBytes(Bitmap image) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return byteStream.toByteArray();
    }

    public static Bitmap bitmapFromBytes(byte[] bytes)
    {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
