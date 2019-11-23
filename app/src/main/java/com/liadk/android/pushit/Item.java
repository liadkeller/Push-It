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

        public boolean inPage() {
            return this == PUBLISHED || this == CREATED;
        }
    }

    private UUID mId;

    private String mTitle = "";
    private String mAuthor = "";
    private boolean mHasImage = false; // has main image
    private Uri mImageUri;
    private Date mOriginalTime; // original creation time
    private Date mTime;                  // creation time
    private long mPublishTime;  // push notification time - implemented as long so when item not published: 0
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
        mPublishTime = 0;

        mState = State.NEW;

        mCounter = 0;
        mTextSegments = new ArrayList<>();
        mMediaSegments = new ArrayList<>();

        mTextSegments.add("");
    }

    private Item() {
        this(null);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    static Item fromDB(DataSnapshot ds) {
        if (ds.getValue() == null) return null;

        Item item = new Item();

        item.mId = UUID.fromString(ds.getKey());
        item.mTitle = (String) ds.child("title").getValue();
        item.mAuthor = (String) ds.child("author").getValue();
        if (ds.child("has-image").getValue() != null)
            item.mHasImage = (boolean) ds.child("has-image").getValue();

        if (ds.child("owner").getValue() != null)
            item.mOwnerId = UUID.fromString((String) ds.child("owner").getValue());
        item.mState = State.getState((String) ds.child("state").getValue());

        item.mCounter = 0;
        item.mTextSegments = new ArrayList<>();
        item.mMediaSegments = new ArrayList<>();
        item.mTime = null;
        item.mOriginalTime = null;
        item.mPublishTime = 0;

        if (ds.child("counter").getValue() != null)
            item.mCounter = ds.child("counter").getValue(Integer.class);
        for (int i = 0; i <= item.mCounter; i++)
            item.mTextSegments.add((String) ds.child("text").child(i + "").getValue());
        for (int i = 0; i < item.mCounter; i++)
            item.mMediaSegments.add(null);     // fill the MediaSegments with nulls (as many as mCounter)

        if (ds.child("time").getValue() != null)
            item.mTime = new Date((long) ds.child("time").getValue());
        if (ds.child("original-time").getValue() != null)
            item.mOriginalTime = new Date((long) ds.child("original-time").getValue());
        if (ds.child("publish-time").getValue() != null)
            item.mPublishTime = (long) ds.child("publish-time").getValue();

        return item;
    }

    void setHasImage(boolean hasImage) {
        this.mHasImage = hasImage;
    }

    void setCurrentTime() {
        mTime = new Date();
    }

    void setPublishTime() {
        setPublishTime(new Date());
    }

    private void setPublishTime(Date time) {
        mPublishTime = time.getTime();
    }

    // promotes mCounter and adds a text segment
    void addImage() {
        mMediaSegments.add(null);
        mTextSegments.add("");
        mCounter++;
    }

    void removeTextSegment(int index) {
        if (index > 0 && index <= mCounter) {
            String linebreak = (mTextSegments.get(index - 1).equals("") || mTextSegments.get(index).equals("")) ? "" : "\n";
            String text = mTextSegments.get(index - 1) + linebreak + mTextSegments.get(index);
            mTextSegments.remove(index);
            mTextSegments.set(index - 1, text);
            mCounter--;
        }
    }

    boolean hasImage() {
        return mHasImage;
    }

    Uri getImageUri() {
        return mImageUri;
    }

    void setImageUri(Uri imageUrl) {
        this.mImageUri = imageUrl;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    Date getOriginalTime() {
        return mOriginalTime;
    }

    String getFormattedOriginalTime() {
        if(mOriginalTime == null) return null;
        return DateFormat.format("MMM d, hh:mm", mOriginalTime).toString();
    }

    public String getText() {
        return mTextSegments.get(0);
    }

    public void setText(String text) {
        mTextSegments.set(0, text);
    }

    String getTime() {
        if(mTime == null) return null;
        return DateFormat.format("MMM d, hh:mm", mTime).toString();
    }

    void setTime(Date time) {
        mTime = time;
    }

    String getShortTime() {
        if(mTime == null) return null;
        return DateFormat.format("hh:mm", mTime).toString();
    }

    String getAuthor() {
        return mAuthor;
    }

    void setAuthor(String author) {
        mAuthor = author;
    }

    String getDetails() {
        if (mAuthor == null || mAuthor.equals("")) return getTime();
        else return getTime() + " | " + mAuthor;
    }

    State getState() {
        return mState;
    }

    void setState(State state) {
        mState = state;
    }

    UUID getOwnerId() {
        return mOwnerId;
    }

    ArrayList<String> getTextSegments() {
        return mTextSegments;
    }

    ArrayList<Uri> getMediaSegments() {
        return mMediaSegments;
    }

    int getSegmentsCounter() {
        return mCounter;
    }

    long getOriginalTimeLong() {
        if(mOriginalTime == null) return 0;
        return mOriginalTime.getTime();
    }

    long getTimeLong() {
        if(mTime == null) return 0;
        return mTime.getTime();
    }

    long getPublishTimeLong() {
        return mPublishTime;
    }
}
