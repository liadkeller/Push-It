package com.liadk.android.pushit;

import android.net.Uri;
import android.text.format.DateFormat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

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
                    return "Draft";
                case CREATED:
                    return "Created";
                case PUBLISHED:
                    return "Published";
                default:
                    return "New";
            }
        }
    }

    UUID mId;

    String mTitle = "";
    String mAuthor = "";
    Uri mImageUri;
    int mOrder;
    Date mOriginalTime; // original creation time
    Date mTime;                  // creation time
    UUID mOwnerId;

    ArrayList<String> mTextSegments;
    ArrayList<Uri> mMediaSegments;

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
        mImageUri = item.mImageUri;
        mAuthor = item.mAuthor;
        mOrder = item.mOrder;
        mOriginalTime = item.mOriginalTime;
        mTime = item.mTime;
        mOwnerId = item.mOwnerId;
        mTextSegments = new ArrayList<>(item.mTextSegments);
        mMediaSegments = new ArrayList<>(item.mMediaSegments);
        mCounter = item.mCounter;

        mState = item.mState;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setImageUri(Uri imageUrl) {
        this.mImageUri = imageUrl;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public void setOrder(int order) {
        this.mOrder = order;
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

    // promotes mCounter and adds a text segment
    public void addImage() {
        mMediaSegments.add(null);
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

    public Uri getImageUri() {
        return mImageUri;
    }

    public String getText() {
        return mTextSegments.get(0);
    }

    public int getOrder() {
        return mOrder;
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

    public ArrayList<Uri> getMediaSegments() {
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
        if (ds.getValue() == null) return null;

        Item item = new Item();

        item.mId = UUID.fromString(ds.getKey());
        item.mTitle = (String) ds.child("title").getValue();
        item.mAuthor = (String) ds.child("author").getValue();

        //if (ds.child("image-url").getValue() != null) // TODO DECIDE!
        //    item.mImageUri = Uri.parse((String) ds.child("image-url").getValue());

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
            item.mTextSegments.add((String) ds.child("text").child(i + "").getValue());

        if(ds.child("time").getValue() != null)
            item.mTime = new Date( (long) ds.child("time").getValue());
        if(ds.child("original-time").getValue() != null)
            item.mOriginalTime = new Date((long) ds.child("original-time").getValue());

        return item;
    }


    public void pushToDB(DatabaseReference db) {
        db.child(getId().toString()).child("title").setValue(getTitle());
        db.child(getId().toString()).child("author").setValue(getAuthor());
        db.child(getId().toString()).child("has-image").setValue(false); // triggers refreshing images after upload to storage
        db.child(getId().toString()).child("time").setValue(getTimeLong());
        db.child(getId().toString()).child("original-time").setValue(getOriginalTimeLong());
        db.child(getId().toString()).child("owner").setValue(getOwnerId().toString());
        db.child(getId().toString()).child("state").setValue(getState().toString());

        db.child(getId().toString()).child("counter").setValue(getSegmentsCounter());
        for(int i = 0; i <= getSegmentsCounter(); i++)
            db.child(getId().toString()).child("text").child(i+"").setValue(getTextSegments().get(i));
    }
}
