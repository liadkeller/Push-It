package com.liadk.android.pushit;

import android.graphics.Bitmap;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class Page {
    UUID mId;
    String mName = "";
    String mDescription = "";
    Bitmap mLogoImage;
    ArrayList<Item> mPageItems;

    PageSettings settings;

    enum Design {
        ALL_HEADERS, SINGLE_HEADER, NO_IMAGES;

        public static Design getDesign(String design) {
            if("All Headers".equals(design))
                return ALL_HEADERS;
            else if("No Images".equals(design))
                return NO_IMAGES;
            else
                return SINGLE_HEADER;
        }

        @Override
        public String toString() {
            if(this == ALL_HEADERS)
                return "All Headers";
            else if (this == NO_IMAGES)
                return "No Images";
            else
                return "Single Header";
        }
    }

    public class PageSettings {
        public Design design;

        public PageSettings() {
            if(design == null) {
                design = Design.SINGLE_HEADER; // Default
            }
        }
    }



    public Page() {
        mId = UUID.randomUUID();
        mPageItems = new ArrayList<>();
        settings = new PageSettings();
    }

    public static Page fromDB(DataSnapshot ds) {
        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());
        page.mName = (String) ds.child("name").getValue();
        page.mDescription = (String) ds.child("description").getValue();
        for(DataSnapshot dataSnapshot : ds.child("items").getChildren()) {
            Item item = Item.fromDB(dataSnapshot, page);
            page.mPageItems.add(item);
            // TODO sort mPageItems according to time
        }

        return page;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public void setLogoImage(Bitmap logoImage) {
        this.mLogoImage = logoImage;
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

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public Bitmap getLogoImage() {
        return mLogoImage;
    }

    public ArrayList<Item> getItems() {
        return mPageItems;
    }
    
    
    public void pushToDB(DatabaseReference db) {
        db.child(getId().toString()).child("name").setValue(getName());
        db.child(getId().toString()).child("description").setValue(getDescription());
        for(Item item : getItems())
            item.pushToDB(db.child(getId().toString()).child("items"));
    }
}
