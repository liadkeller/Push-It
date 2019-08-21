package com.liadk.android.pushit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by user on 11/08/2019.
 */
public class PageCollection {
    private static PageCollection sPageCollection;
    private Context mAppContext;

    private ArrayList<Page> mPages;

    public PageCollection(Context appContext) {
        mAppContext = appContext;
        mPages = new ArrayList<>();


        Page page = new Page();
        ArrayList<Item> pageItems = new ArrayList<>();

        page.setItems(pageItems);
        mPages.add(page);


        Item sirenBreaking = new Item(page); // TODO Delete this
        sirenBreaking.setTitle("Siren Shakes City of Sderot Overnight");
        sirenBreaking.setAuthor("Lidi");
        sirenBreaking.setText("A Red Alert siren has been activated after 3 missles were launched into the city of Sderot and nearby settlements from the Gaza Strip.");
        sirenBreaking.setState(Item.State.CREATED);
        Bitmap img = BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.rockets_gaza);
        sirenBreaking.setImage(img);

        ItemCollection.get(mAppContext).add(sirenBreaking);
        pageItems.add(sirenBreaking);


        Item gazansBreaking = new Item(page);
        gazansBreaking.setTitle("Officials: Israel Transferring Gazans is 'Seriously Considered'");
        gazansBreaking.setText("Israel is actively promoting the emigration of Palestinians from the Gaza Strip, and is working to find other countries who may be willing to absorb them, a senior Israeli official said Monday." +
                "\n\nIsrael is ready to carry the costs of helping Gazans emigrate, and would even be willing to consider allowing them to use an Israeli air field close to Gaza to allow them to leave for their new host countries, the official said, apparently referring to air force bases deep inside Israel.");
        gazansBreaking.setState(Item.State.CREATED);
        Bitmap img2 = BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.netanyahu);
        gazansBreaking.setImage(img2);

        ItemCollection.get(mAppContext).add(gazansBreaking);
        pageItems.add(gazansBreaking);


        Item tlaibBreaking = new Item(page);
        tlaibBreaking.setTitle("Trump Mocks Tlaib's Grandma as She \"Don't Have to See Her Now\"");
        tlaibBreaking.setState(Item.State.CREATED);
        Bitmap img3 = BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.tlaibvstrump);
        tlaibBreaking.setImage(img3);

        ItemCollection.get(mAppContext).add(tlaibBreaking);
        pageItems.add(tlaibBreaking);
    }

    public static PageCollection get(Context c) {
        if(sPageCollection == null) {
            sPageCollection = new PageCollection(c.getApplicationContext());
        }

        return sPageCollection;
    }

    public ArrayList<Page> getPages() {
        return mPages;
    }

    public Page getPage(UUID id) {
        for(Page page: mPages) {
            if(page.getId().equals(id))
                return page;
        }
        return null;
    }

    public void add(Page page) {
        mPages.add(page);
    }

    public void delete(Page page) {
        mPages.remove(page);
    }
}
