package com.liadk.android.pushit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.UUID;

public class ExploreFragment extends Fragment {

    FirebaseDatabase mDatabase;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.explore);
        mDatabase = FirebaseDatabase.getInstance();

        resetDatabase();
        addDataToDatabase();
    }

    private void resetDatabase() {
        DatabaseReference pagesDatabase = mDatabase.getReference("pages");
        DatabaseReference itemsDatabase = mDatabase.getReference("items");

        pagesDatabase.setValue(null);
        itemsDatabase.setValue(null);
    }

    private void addDataToDatabase() { // TODO GET RID OF
        DatabaseReference pagesDatabase = mDatabase.getReference("pages");
        DatabaseReference itemsDatabase = mDatabase.getReference("items");

        for(Page page : PageCollection.get(getActivity()).getPages())
            page.pushToDB(pagesDatabase);

        for(Item item : ItemCollection.get(getActivity()).getItems())
            item.pushToDB(itemsDatabase);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new PageListRecycleViewAdapter(getActivity()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        return v;
    }

    public static Fragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ItemFragment.EXTRA_ID, id);

        ExploreFragment fragment = new ExploreFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public class PageListRecycleViewAdapter extends RecyclerView.Adapter {
        Context mContext;
        ArrayList<Page> mPages;

        PageListRecycleViewAdapter(Context context) {
            mContext = context;
            mPages = PageCollection.get(mContext).getPages();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_page_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final Page page = mPages.get(position);
            ((ViewHolder)holder).mNameTextView.setText(page.getName());
            ((ViewHolder)holder).mDescTextView.setText(page.getDescription());
            ((ViewHolder)holder).mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), PageActivity.class);
                    intent.putExtra(PageFragment.EXTRA_ID, page.getId());
                    startActivity(intent);
                }
            });

            if(page.getLogoImage() != null) // TODO Take care of default logo (including default Bibi image in the xml layout file)
                ((ViewHolder)holder).mImageView.setImageBitmap(page.getLogoImage());
        }

        @Override
        public int getItemCount() {
            return mPages.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout mLinearLayout;
            ImageView mImageView;
            TextView mNameTextView;
            TextView mDescTextView;

            ViewHolder(View v) {
                super(v);
                mLinearLayout = (LinearLayout) v.findViewById(R.id.pageListItemLayout);
                mImageView = (ImageView) v.findViewById(R.id.pageImageView);
                mNameTextView = (TextView) v.findViewById(R.id.pageNameTextView);
                mDescTextView = (TextView) v.findViewById(R.id.pageDescTextView);
            }
        }
    }
}
