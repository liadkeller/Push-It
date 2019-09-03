package com.liadk.android.pushit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

public class PageFragment extends Fragment {

    private static final String TAG = "PageFragment";
    public static final String EXTRA_ID = "pageId";

    private static final int HEADER_TYPE = 0;
    private static final int NO_HEADER_TYPE = 1;
    private static final int NO_IMAGE_TYPE = 2;

    private Page mPage;
    private DatabaseReference mPagesDatabase;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);

        mPagesDatabase = FirebaseDatabase.getInstance().getReference("pages");
        //mPagesDatabase.child(id.toString()).child("items").orderByChild("time"); TODO Sort Items By Time
        mPagesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPage = Page.fromDB(dataSnapshot.child(id.toString()));
                onPageChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void onPageChanged() {
        if(!"".equals(mPage.getName()) && getActivity() != null) {
            getActivity().setTitle(mPage.getName());
        }

        Log.d(TAG, "Page items number: " + mPage.getItems().size());

        if(mRecyclerView != null)
            ((PageRecycleViewAdapter) mRecyclerView.getAdapter()).setPage(mPage);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view_refresh, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new PageRecycleViewAdapter(getActivity()));
        if(mPage != null)
            ((PageRecycleViewAdapter) mRecyclerView.getAdapter()).setPage(mPage);

        final int PADDING_SIZE = (int) (4 * (getResources().getDisplayMetrics().density) + 0.5f); // 4dp
        mRecyclerView.setPadding(0, PADDING_SIZE, 0, PADDING_SIZE);

        mSwipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO Refresh
                mRecyclerView.getAdapter().notifyDataSetChanged();
                mSwipeRefresh.setRefreshing(false);
            }
        });

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


        public PageRecycleViewAdapter(Context context) {
            mContext = context;
        }

        public void setPage(Page page) {
            mItems = page.getItems();
            notifyDataSetChanged();
        }

        // Returns the item of the menu of whose menu item was selected
        private Item getItem(MenuItem menuItem) {
            if(mItems == null) return null;

            int position = menuItem.getOrder(); // we put the adapter position instead of the order
            int reversedIndex = mItems.size() - 1 - position;

            return mItems.get(reversedIndex);
        }

        @Override
        public int getItemViewType(int position) {
            if(mPage.settings.design == Page.Design.NO_IMAGES)
                return NO_IMAGE_TYPE;

            if(mPage.settings.design == Page.Design.SINGLE_HEADER && position > 0)
                return NO_HEADER_TYPE;

            else
                return HEADER_TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            if(viewType == HEADER_TYPE) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_card_header, parent, false);
                return new HeaderViewHolder(v);
            }

            else if(viewType == NO_HEADER_TYPE) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_card, parent, false);
                return new NoHeaderViewHolder(v);
            }

            else { // if(view type == NO_IMAGES)
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_card_no_image, parent, false);
                return new NoImageViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int reversedIndex = mItems.size() - 1 - position;
            final Item item = mItems.get(reversedIndex);

            if(getItemViewType(position) == HEADER_TYPE) {
                HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
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
            }

            else if(getItemViewType(position) == NO_HEADER_TYPE) {
                NoHeaderViewHolder viewHolder = (NoHeaderViewHolder) holder;
                viewHolder.mImageView.setImageBitmap(item.getImage());
                viewHolder.mTextView.setText(item.getTitle());
                viewHolder.mTimeTextView.setText(item.getShortTime());
                viewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), ItemActivity.class);
                        i.putExtra(ItemFragment.EXTRA_ID, item.getId());
                        startActivity(i);
                    }
                });
            }

            if(getItemViewType(position) == NO_IMAGE_TYPE){

                NoImageViewHolder viewHolder = (NoImageViewHolder) holder;
                viewHolder.mTextView.setText(item.getTitle());
                viewHolder.mTimeTextView.setText(item.getShortTime());
                viewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), ItemActivity.class);
                        i.putExtra(ItemFragment.EXTRA_ID, item.getId());
                        startActivity(i);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if(mItems == null) return 0;
            return mItems.size();
        }

        private class HeaderViewHolder extends RecyclerView.ViewHolder {
            CardView mCardView;
            ImageView mImageView;
            TextView mTextView;

            public HeaderViewHolder(View itemView) {
                super(itemView);

                mCardView = (CardView) itemView.findViewById(R.id.card);
                mImageView = (ImageView) itemView.findViewById(R.id.cardImageView);
                mTextView = (TextView) itemView.findViewById(R.id.cardText);

                mCardView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                        contextMenu.add(0, R.id.context_menu_edit_item, getAdapterPosition(), R.string.edit_item); // we put adapter position instead of 'item order'
                    }
                });
            }
        }

        private class NoHeaderViewHolder extends RecyclerView.ViewHolder {
            CardView mCardView;
            ImageView mImageView;
            TextView mTextView;
            TextView mTimeTextView;

            public NoHeaderViewHolder(View itemView) {
                super(itemView);

                mCardView = (CardView) itemView.findViewById(R.id.card);
                mImageView = (ImageView) itemView.findViewById(R.id.cardImageView);
                mTextView = (TextView) itemView.findViewById(R.id.cardText);
                mTimeTextView = (TextView) itemView.findViewById(R.id.cardTime);

                mCardView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                        contextMenu.add(0, R.id.context_menu_edit_item, getAdapterPosition(), R.string.edit_item); // we put adapter position instead of 'item order'
                    }
                });
            }
        }

        private class NoImageViewHolder extends RecyclerView.ViewHolder {
            CardView mCardView;
            TextView mTextView;
            TextView mTimeTextView;


            public NoImageViewHolder(View v) {
                super(v);

                mCardView = (CardView) v.findViewById(R.id.card);
                mTextView = (TextView) v.findViewById(R.id.cardText);
                mTimeTextView = (TextView) v.findViewById(R.id.cardTime);

                mCardView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                        contextMenu.add(0, R.id.context_menu_edit_item, getAdapterPosition(), R.string.edit_item); // we put adapter position instead of 'item order'
                    }
                });
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
            DatabaseReference itemsDatabase = FirebaseDatabase.getInstance().getReference("items");

            Item newItem = new Item(mPage.getId());
            newItem.pushToDB(itemsDatabase); // we add only to itemsDatabase and not to pagesDatabase - we'll add it there when we save the item in EditItemFragment

            Intent intent = new Intent(getActivity(), EditItemActivity.class);
            intent.putExtra(ItemFragment.EXTRA_ID, newItem.getId());
            startActivity(intent);
            return true;
        }

        else if(item.getItemId() == R.id.menu_item_page_settings) {

            Intent i = new Intent(getActivity(), PageSettingsActivity.class);
            i.putExtra(EXTRA_ID, mPage.getId());
            startActivity(i);
            return true;
        }

        else if(item.getItemId() == android.R.id.home) {

            if (NavUtils.getParentActivityName(getActivity()) != null) {
                NavUtils.navigateUpFromSameTask(getActivity());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // takes care of context menu for editing items
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.context_menu_edit_item) {
            Item item = ((PageRecycleViewAdapter) mRecyclerView.getAdapter()).getItem(menuItem);

            Intent intent = new Intent(getActivity(), EditItemActivity.class);
            intent.putExtra(ItemFragment.EXTRA_ID, item.getId());
            startActivity(intent);

            return true;
        }

        return super.onContextItemSelected(menuItem);
    }


    public static Fragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ItemFragment.EXTRA_ID, id);

        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);

        return (Fragment) fragment;
    }
}
