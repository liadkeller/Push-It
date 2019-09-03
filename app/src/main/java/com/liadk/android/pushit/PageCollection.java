package com.liadk.android.pushit;

import android.content.Context;
import android.graphics.BitmapFactory;

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

        // TODO Delete From Here:
        Page page = new Page();
        //Page page2 = new Page();
        ArrayList<Item> pageItems = new ArrayList<>();

        page.setItems(pageItems);
        page.setName("Lidi News - Best News in Town");
        page.setDescription("Lidi's here for ya");
        mPages.add(page);

        
/*
        page2.setItems(pageItems);
        page2.setName("Breaking News All Night");
        page2.setDescription("Here to Break it everything right as it comes");
        page2.settings.design = Page.Design.NO_IMAGES;
        mPages.add(page2);

*/




        Item bibiBreaking = new Item(page.getId());
        bibiBreaking.setTitle("Opinion: Netanyahu Gains Victory in the North, Humilliated in the South");
        bibiBreaking.setImage(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.bibi_face));

        ItemCollection.get(mAppContext).add(bibiBreaking);
        page.addItem(bibiBreaking);



        Item sderotBreaking = new Item(page.getId());
        sderotBreaking.setTitle("Siren Strikes Fear in Sderot During Massive City Event");
        sderotBreaking.setImage(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.iron_dome));

        ItemCollection.get(mAppContext).add(sderotBreaking);
        page.addItem(sderotBreaking);

        
/*
        Item tlaibBreaking = new Item(page.getId());
        tlaibBreaking.setTitle("Trump Mocks Tlaib's Grandma as She \"Don't Have to See Her Now\"");
        tlaibBreaking.setState(Item.State.CREATED);
        Bitmap img3 = BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.tlaibvstrump);
        tlaibBreaking.setImage(img3);

        ItemCollection.get(mAppContext).add(tlaibBreaking);
        pageItems.add(tlaibBreaking);

        

        Item sirenBreaking = new Item(page.getId());
        sirenBreaking.setTitle("Siren Shakes City of Sderot Overnight");
        sirenBreaking.setAuthor("Lidi");
        sirenBreaking.setText("A Red Alert siren has been activated after 3 missles were launched into the city of Sderot and nearby settlements from the Gaza Strip.");
        sirenBreaking.setState(Item.State.CREATED);
        Bitmap img = BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.rockets_gaza);
        sirenBreaking.setImage(img);

        ItemCollection.get(mAppContext).add(sirenBreaking);
        pageItems.add(sirenBreaking);

        

        Item gazansBreaking = new Item(page.getId());
        gazansBreaking.setTitle("Officials: Israel Transferring Gazans is 'Seriously Considered'");
        gazansBreaking.setText("Israel is actively promoting the emigration of Palestinians from the Gaza Strip, and is working to find other countries who may be willing to absorb them, a senior Israeli official said Monday." +
                "\n\nIsrael is ready to carry the costs of helping Gazans emigrate, and would even be willing to consider allowing them to use an Israeli air field close to Gaza to allow them to leave for their new host countries, the official said, apparently referring to air force bases deep inside Israel.");
        gazansBreaking.setState(Item.State.CREATED);
        Bitmap img2 = BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.netanyahu);
        gazansBreaking.setImage(img2);

        ItemCollection.get(mAppContext).add(gazansBreaking);
        pageItems.add(gazansBreaking);

        

        Item blueWhiteBreaking = new Item(page.getId());
        blueWhiteBreaking.setTitle("B&W Member Draws Criticism After Stupid Remarks");
        blueWhiteBreaking.setImage(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.ben_barak));

        ItemCollection.get(mAppContext).add(blueWhiteBreaking);
        page.addItem(blueWhiteBreaking);

        

        Item walshBreaking = new Item(page.getId());
        walshBreaking.setTitle("Trump is Terrified:\nJoe Walsh Running For President");
        walshBreaking.setImage(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.joe_walsh));

        ItemCollection.get(mAppContext).add(walshBreaking);
        page.addItem(walshBreaking);

        Item laborBreaking = new Item(page.getId());
        laborBreaking.setTitle("MASSIVE DRAMA:\nLabor Leader Sheds Iconic Mustache After 47 Years");
        laborBreaking.setImage(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.peretz_bibi));

        ItemCollection.get(mAppContext).add(laborBreaking);
        page.addItem(laborBreaking);

        

        Item sulimaniBreaking = new Item(page.getId());
        sulimaniBreaking.setTitle("IDF Eliminates Threat of Drones Attack Hours Before Executed");
        sulimaniBreaking.setImage(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.sulimani));

        ItemCollection.get(mAppContext).add(sulimaniBreaking);
        page.addItem(sulimaniBreaking);

        Item macronBreaking = new Item(page.getId());
        macronBreaking.setTitle("Haley Slams Macron: 'Manipulative', 'Insincere' & 'Completely Disrespectful'");
        macronBreaking.setImage(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.haley));

        ItemCollection.get(mAppContext).add(macronBreaking);
        page.addItem(macronBreaking);

        // TODO Up to Here!   */
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
