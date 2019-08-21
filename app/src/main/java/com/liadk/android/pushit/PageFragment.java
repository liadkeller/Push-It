package com.liadk.android.pushit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

public class PageFragment extends Fragment {

    private static final String TAG = "PageFragment";

    private Page mPage;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mPage = PageCollection.get(getActivity()).getPages().get(0); // TODO Delete This
        Log.d(TAG, "Page items number: " + mPage.getItems().size());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setAdapter(new PageRecycleViewAdapter(getActivity(), mPage));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private class PageRecycleViewAdapter extends RecyclerView.Adapter {
        Context mContext;
        ArrayList<Item> mItems;


        public PageRecycleViewAdapter(Context context, Page page) {
            mContext = context;
            mItems = page.getItems();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int reversedIndex = mItems.size() - 1 - position;
            final Item item = mItems.get(reversedIndex);

            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.mImageView.setImageBitmap(item.getImage());
            viewHolder.mTextView.setText(item.getTitle());
            viewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), ItemActivity.class);
                    i.putExtra(ItemFragment.EXTRA_ID, item.getId());
                    startActivity(i);
                }
            });
            /*
            viewHolder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Boolean isPremitted; // TODO take care of user permissions
                    if(isPremitted = true) {
                        TextView editButton = new TextView(getActivity());
                        editButton.setText(R.string.edit_item);
                        editButton.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        //editButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        editButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getActivity(), ItemActivity.class);
                                i.putExtra(ItemFragment.EXTRA_ID, item.getId());
                                startActivity(i);
                            }
                        });

                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(editButton).create();
                        alertDialog.show();
                        return true;
                    }
                    return false;
                }
            });
             */
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            CardView mCardView;
            ImageView mImageView;
            TextView mTextView;

            public ViewHolder(View itemView) {
                super(itemView);

                mCardView = (CardView) itemView.findViewById(R.id.card);
                mImageView = (ImageView) itemView.findViewById(R.id.cardImageView);
                mTextView = (TextView) itemView.findViewById(R.id.cardText);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_page, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_create_article) {

            Item newItem = new Item(mPage);
            ItemCollection.get(getActivity()).add(newItem);

            Intent intent = new Intent(getActivity(), EditItemActivity.class);
            intent.putExtra(ItemFragment.EXTRA_ID, newItem.getId());
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
