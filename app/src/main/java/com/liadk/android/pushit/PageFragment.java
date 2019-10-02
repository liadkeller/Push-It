package com.liadk.android.pushit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class PageFragment extends Fragment {

    private static final String TAG = "PageFragment";
    public static final String EXTRA_ID = "pageId";

    private static final int HEADER_TYPE = 0;
    private static final int NO_HEADER_TYPE = 1;
    private static final int NO_IMAGE_TYPE = 2;

    private Page mPage;
    private ArrayList<Item> mItems;
    private PushItUser mUser;
    private String mUserId;

    private FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;
    private ValueEventListener mDatabaseListener;

    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private SwipeRefreshLayout mSwipeRefresh;
    private Button mAddArticleButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("");

        final UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseManager = DatabaseManager.get(getActivity());
        mDatabaseListener = mDatabaseManager.addDatabaseListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPage = Page.fromDB(dataSnapshot.child("pages").child(id.toString()));

                if(dataSnapshot.child("pages").child(id.toString()).getValue() == null) {
                    Toast.makeText(getActivity(), R.string.page_not_exist, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    return;
                }

                mItems = getItems(mPage.getItemsIdentifiers(), dataSnapshot);

                if(mAuth.getCurrentUser() != null) {
                    mUserId = mAuth.getCurrentUser().getUid();
                    mUser = dataSnapshot.child("users").child(mUserId).getValue(PushItUser.class);
                }

                if(mRecyclerView != null)
                    configureAdapter(mItems);

                onPageChanged();
            }

            // gets a list of items IDs and a snapshot of the db and returns a list of the actual items
            private ArrayList<Item> getItems(ArrayList<UUID> itemsIdentifiers, DataSnapshot dataSnapshot) {
                ArrayList<Item> items = new ArrayList<>();
                for (UUID id : itemsIdentifiers) {
                    Item item = Item.fromDB(dataSnapshot.child("items").child(id.toString()));
                    items.add(item);
                }

                Collections.sort(items, new Comparator<Item>() {
                    @Override
                    public int compare(Item i1, Item i2) {
                        if (i1 == null && i2 == null) return 0;
                        else if (i1 == null) return 1;
                        else if (i2 == null) return -1;
                        return (int) (i1.getTimeLong() - i2.getTimeLong());
                    }
                });

                return items;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void onPageChanged() {
        if(!"".equals(mPage.getName()) && getActivity() != null) {
            getActivity().setTitle(mPage.getName());
        }

        Log.d(TAG, "Page items number: " + mPage.getItemsIdentifiers().size());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page, container, false);

        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyView = v.findViewById(R.id.noUserView);
        mAddArticleButton = v.findViewById(R.id.addArticleButton);
        mAddArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewItem();
            }
        });

        final PageRecycleViewAdapter adapter = new PageRecycleViewAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);

        if(mItems != null)
            configureAdapter(mItems);

        final int PADDING_SIZE = (int) (4 * (getResources().getDisplayMetrics().density) + 0.5f); // 4dp
        mRecyclerView.setPadding(0, PADDING_SIZE, 0, PADDING_SIZE);


        mSwipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRecyclerView.getAdapter().notifyDataSetChanged();
                mSwipeRefresh.setRefreshing(false);
            }
        });

        return v;
    }

    private void configureAdapter(ArrayList<Item> items) {

        final PageRecycleViewAdapter adapter = (PageRecycleViewAdapter) mRecyclerView.getAdapter();

        adapter.setItems(items);
        mEmptyView.setVisibility(items.size() == 0 ? View.VISIBLE : View.GONE);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() { // checks if recycler view empty

            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            void checkEmpty() {
                mEmptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDatabaseManager.removeDatabaseListener(mDatabaseListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mDatabaseListener != null)
            mDatabaseManager.removeDatabaseListener(mDatabaseListener);
    }

    private class PageRecycleViewAdapter extends RecyclerView.Adapter {
        Context mContext;
        ArrayList<Item> mItems;


        public PageRecycleViewAdapter(Context context) {
            mContext = context;
        }

        public void setItems(ArrayList<Item> items) {
            mItems = items;
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

                loadItemImage(item, viewHolder.mImageView);
                viewHolder.mTextView.setText(item.getTitle());
                viewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), ItemActivity.class);
                        i.putExtra(ItemFragment.EXTRA_ID, item.getId());
                        startActivity(i);
                    }
                });

                // Item Status Toast - Should be limited to page owner only
                viewHolder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(mContext, "Article Status: " + item.getState().toString(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }

            else if(getItemViewType(position) == NO_HEADER_TYPE) {
                NoHeaderViewHolder viewHolder = (NoHeaderViewHolder) holder;

                loadItemImage(item, viewHolder.mImageView);
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

                // Item Status Toast - Should be limited to page owner only
                viewHolder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(mContext, "Article Status: " + item.getState().toString(), Toast.LENGTH_SHORT).show();
                        return true;
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

                // Item Status Toast - Should be limited to page owner only
                viewHolder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(mContext, "Article Status: " + item.getState().toString(), Toast.LENGTH_SHORT).show();
                        return true;
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

    private void loadItemImage(final Item item, final ImageView imageView) {
        Task loadingTask = FirebaseStorage.getInstance().getReference("items").child(item.getId().toString()).child("image.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(getActivity() == null) return;

                if(task.getException() == null) {
                    item.setImageUri(task.getResult());
                    Glide.with(getActivity()).load(item.getImageUri()).into(imageView);

                    imageView.setVisibility(View.VISIBLE);
                }

                else
                    imageView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_page, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(mUser != null) {
            MenuItem followMenuItem = menu.findItem(R.id.menu_item_follow_page);
            if (followMenuItem != null)
                followMenuItem.setTitle(mPage.hasFollowedBy(mUserId) ? R.string.unfollow_page : R.string.follow_page);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_create_article)
            return createNewItem();

        else if(item.getItemId() == R.id.menu_item_page_settings) {
            Intent i = new Intent(getActivity(), PageSettingsActivity.class);
            i.putExtra(EXTRA_ID, mPage.getId());
            startActivity(i);
            return true;
        }

        else if(item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }

        else if(item.getItemId() == R.id.menu_item_follow_page) {
            if(mAuth.getCurrentUser() == null || mUser == null || mUserId == null || !mAuth.getCurrentUser().getUid().equals(mUserId)) return false;

            if(item.getTitle().toString().equals(getString(R.string.follow_page)))
                mPage.addNewFollower(mUser, mUserId);

            else if (item.getTitle().toString().equals(getString(R.string.unfollow_page)))
                mPage.removeFollower(mUser, mUserId);

            else
                return false;

            mDatabaseManager.followPage(mUser, mUserId, mPage);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean createNewItem() {
        Item newItem = new Item(mPage.getId());
        mDatabaseManager.pushItemToDB(newItem); // we add only to itemsDatabase and not to pagesDatabase - we'll add it there when we save the item in EditItemFragment

        Intent intent = new Intent(getActivity(), EditItemActivity.class);
        intent.putExtra(ItemFragment.EXTRA_ID, newItem.getId());
        startActivity(intent);
        return true;
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
