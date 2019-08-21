package com.liadk.android.pushit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class Page {
    UUID mId;
    ArrayList<Item> mPageItems;

    public Page() {
        mId = UUID.randomUUID();
        mPageItems = new ArrayList<>();
    }

    public void setItems(ArrayList<Item> mPageItems) {
        this.mPageItems = mPageItems;
    }

    public void addItem(Item item) {
        mPageItems.add(item);
    }

    public void removeItem(Item item) {
        mPageItems.remove(item);
    }

    public boolean has(Item item) {
        return mPageItems.contains(item);
    }

    public UUID getId() {
        return mId;
    }

    public ArrayList<Item> getItems() {
        return mPageItems;
    }
}
