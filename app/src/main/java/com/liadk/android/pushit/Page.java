package com.liadk.android.pushit;

import android.net.Uri;

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
    Uri mLogoImageUrl;
    ArrayList<UUID> mItemsIdentifiers;

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
        mItemsIdentifiers = new ArrayList<>();
        settings = new PageSettings();
    }

    // only details needed for explore/follow tabs
    public static Page getPageDetailsFromDB(DataSnapshot ds) {
        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());
        page.mName = (String) ds.child("name").getValue();
        page.mDescription = (String) ds.child("description").getValue();
        return page;
    }

    // details needed for page setting screen
    public static Page getPageSettingsFromDB(DataSnapshot ds) {
        Page page = getPageDetailsFromDB(ds);
        page.settings.design = Design.getDesign((String) ds.child("settings").child("design").getValue());

        return page;
    }

    public static Page fromDB(DataSnapshot ds) {
        Page page = new Page();
        page.mId = UUID.fromString(ds.getKey());
        page.mName = (String) ds.child("name").getValue();
        page.mDescription = (String) ds.child("description").getValue();
        for(DataSnapshot dataSnapshot : ds.child("items").getChildren()) {
            UUID itemId = UUID.fromString(dataSnapshot.getKey());
            page.mItemsIdentifiers.add(itemId);
        }
        page.settings.design = Design.getDesign((String) ds.child("settings").child("design").getValue());

        // TODO sort mItemsIdentifiers according to time
        /*Collections.sort(page.mItemsIdentifiers, new Comparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                if(i1 == null && i2 == null) return 0;
                else if (i1 != null) return 1;
                else if (i2 != null) return -1;
                return (int) (i1.getTimeLong() - i2.getTimeLong());
            }
        }); */

        return page;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public void setLogoImageUrl(Uri logoImageUrl) {
        this.mLogoImageUrl = logoImageUrl;
    }

    public void setItemsIdentifiers(ArrayList<UUID> itemsIdentifiers) {
        this.mItemsIdentifiers = itemsIdentifiers;
    }

    public void addItem(UUID id) {
        mItemsIdentifiers.add(id);
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

    public Uri getLogoImageUrl() {
        return mLogoImageUrl;
    }

    public ArrayList<UUID> getItemsIdentifiers() {
        return mItemsIdentifiers;
    }
    
    
    public void pushToDB(DatabaseReference db) {
        db.child(getId().toString()).child("name").setValue(getName());
        db.child(getId().toString()).child("description").setValue(getDescription());
        for(UUID itemId : getItemsIdentifiers())
            db.child(getId().toString()).child("items").child(itemId.toString()).setValue(true);
        db.child(getId().toString()).child("settings").child("design").setValue(settings.design.toString());
    }
}
