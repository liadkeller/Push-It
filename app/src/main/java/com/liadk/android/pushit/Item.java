package com.liadk.android.pushit;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class Item {

    protected enum State {NEW, DRAFT, CREATED, PUBLISHED};

    UUID mId;

    boolean mEdit; // true - edit mode; false - public mode
    Item mEditItem;

    String mTitle;
    Bitmap mImage;
    String mAuthor;
    Date mOriginalTime; // original creation time
    Date mTime;                  // creation time
    Page mOwner;

    ArrayList<String> mTextSegments;
    ArrayList<Object> mMediaSegments;
    //ArrayList<Bitmap> mImages;
    //ArrayList<MediaStore.Video> mVideos;
    int mCounter;

    State mState;


    public Item(Page owner) {
        mId = UUID.randomUUID();
        mOwner = owner;

        mOriginalTime = new Date();
        mTime = mOriginalTime;

        mState = State.NEW;

        mCounter = 0;
        mTextSegments = new ArrayList<>();
        mMediaSegments = new ArrayList<>();
        //mImages = new ArrayList<>();
        //mVideos = new ArrayList<>();

        mTextSegments.add("");

        mEdit = false;
        setNewEditItem(); // If item is edited not through "mEditItem" and the changes weren't synced with mEditItem, they won't appear in Edit mode
    }

    private Item() {
        this((Page) null);
    }

    public Item(Item item) {
        mId = UUID.randomUUID();

        mTitle = item.mTitle;
        mImage = item.mImage;
        mAuthor = item.mAuthor;
        mOriginalTime = item.mOriginalTime;
        mTime = item.mTime;
        mOwner = item.mOwner;
        mTextSegments = new ArrayList<>(item.mTextSegments);
        mMediaSegments = new ArrayList<>(item.mMediaSegments);
        //mImages = item.mImages;
        //mVideos = item.mVideos;
        mCounter = item.mCounter;

        mState = item.mState;
    }

    public void setTitle(String title) {
        mTitle = title;
        if(!mEdit) mEditItem.setTitle(title);
    }

    public void setImage(Bitmap image) {
        mImage = image;
        if(!mEdit) mEditItem.setImage(image);

    }

    public void setAuthor(String author) {
        mAuthor = author;
        if(!mEdit) mEditItem.setAuthor(author);

    }

    public void setTime(Date time) {
        mTime = time;
        if(!mEdit) mEditItem.setTime(time);
    }

    public void setCurrentTime() {
        mTime = new Date();
        if(!mEdit) mEditItem.setTime(mTime);
    }

    public void setText(String text) {
        mTextSegments.set(0, text);
        if(!mEdit) mEditItem.setText(text);
    }

    public void setState(State state) {
        mState = state;
        if(!mEdit) mEditItem.setState(state);
    }

    public void setEdit(boolean edit) {
        this.mEdit = edit;
    }

    public void setEditItem(Item editItem) {
        mEditItem = editItem;
        mEditItem.setEdit(true);
    }

    public void setNewEditItem() {
        setEditItem(new Item(this));
    }

    public void addText(String text) {
        mTextSegments.add(text);
        if(!mEdit) mEditItem.addText(text);
    }

    public void addImage(Bitmap image) {
        mMediaSegments.add(image);
        mTextSegments.add("");
        mCounter++;
        if(!mEdit) mEditItem.addImage(image);
    }

    public void addVideo(MediaStore.Video video) {
        mMediaSegments.add(video);
        mTextSegments.add("");
        mCounter++;
        if(!mEdit) mEditItem.addVideo(video);
    }

    public void setCounter(int counter) {
        mCounter = counter;
    }

    public void addToCounter() {
        mCounter++;
    }

    public void resetSegments() {
        if(mTextSegments.size() > 1) {
            mTextSegments.removeAll(mTextSegments.subList(1, mTextSegments.size()));
        }
        mMediaSegments = new ArrayList<>();
        mCounter = 0;
    }

    public void removeTextSegment(int index) {
        if(index > 0 && index <= mCounter) {
            String linebreak = (mTextSegments.get(index - 1).equals("") || mTextSegments.get(index).equals("")) ? "": "\n";
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

    protected Date getOriginalTime() { return mOriginalTime; }

    public String getFormattedOriginalTime() {
        DateFormat df = new DateFormat();
        return df.format("MMM d, hh:mm", mOriginalTime).toString();
    }

    public String getTime() {
        DateFormat df = new DateFormat();
        return df.format("MMM d, hh:mm", mTime).toString();
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getDetails() {
        if(mAuthor == null || "".equals(mAuthor)) return getTime();
        else return getTime() + " | " + mAuthor;
    }

    public State getState() {
        return mState;
    }

    public Page getOwner() {
        return mOwner;
    }

    public boolean isEdit() {
        return mEdit;
    }

    public Item getEditItem() {
        return mEditItem;
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

    // saves all changes made in the edit screen
    // copies all data from the "edit" copy to this one
    public void edit() {
        mTitle = mEditItem.mTitle;
        mImage = mEditItem.mImage;
        mAuthor = mEditItem.mAuthor;
        mOriginalTime = mEditItem.mOriginalTime;
        mTime = mEditItem.mTime;
        mTextSegments = new ArrayList<>(mEditItem.mTextSegments);
        mMediaSegments = new ArrayList<>(mEditItem.mMediaSegments);
        //mImages = mEditItem.mImages;
        //mVideos = mEditItem.mVideos;
        mCounter = mEditItem.mCounter;
        mOwner = mEditItem.mOwner;

        mState = mEditItem.mState;
    }

    public void addToPage(State oldState) {
        if(mEdit) return;

        if(oldState == State.DRAFT)
            mOwner.removeItem(this); // if saved as a draft

        if(!mOwner.has(this))
            mOwner.addItem(this);
    }
}
