package com.liadk.android.pushit;

import android.net.Uri;
import android.text.format.DateFormat;

import com.google.firebase.database.DataSnapshot;

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

    private UUID mId;

    private String mTitle = "";
    private String mAuthor = "";
    private Uri mImageUri;
    private int mOrder;
    private Date mOriginalTime; // original creation time
    private Date mTime;                  // creation time
    private UUID mOwnerId;

    private ArrayList<String> mTextSegments;
    private ArrayList<Uri> mMediaSegments;

    private int mCounter;

    private State mState;


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


    public static Item fromDB(DataSnapshot ds) {
        if (ds.getValue() == null) return null;

        Item item = new Item();

        item.mId = UUID.fromString(ds.getKey());
        item.mTitle = (String) ds.child("title").getValue();
        item.mAuthor = (String) ds.child("author").getValue();

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
        for(int i = 0; i < item.mCounter; i++)
            item.mMediaSegments.add(null);     // fill the MediaSegments with nulls (as many as mCounter)

        if(ds.child("time").getValue() != null)
            item.mTime = new Date( (long) ds.child("time").getValue());
        if(ds.child("original-time").getValue() != null)
            item.mOriginalTime = new Date((long) ds.child("original-time").getValue());

        return item;
    }
}
