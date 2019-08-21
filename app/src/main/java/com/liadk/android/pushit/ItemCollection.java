package com.liadk.android.pushit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by user on 11/08/2019.
 */
public class ItemCollection {
    private static ItemCollection sItemCollection;
    private Context mAppContext;

    private ArrayList<Item> mItems;

    public ItemCollection(Context appContext) {
        mAppContext = appContext;
        mItems = new ArrayList<>();
    }

    public static ItemCollection get(Context c) {
        if(sItemCollection == null) {
            sItemCollection = new ItemCollection(c.getApplicationContext());
        }

        return sItemCollection;
    }

    public ArrayList<Item> getItems() {
        return mItems;
    }

    public Item getItem(UUID id) {
        for(Item item: mItems) {
            if(item.getId().equals(id))
                return item;
        }
        return null;
    }

    public void add(Item item) {
        mItems.add(item);
    }

    public void delete(Item item) {
        mItems.remove(item);
    }
}
