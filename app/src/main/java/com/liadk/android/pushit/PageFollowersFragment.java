package com.liadk.android.pushit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PageFollowersFragment extends Fragment {

    private Page mPage;
    private LinkedHashMap<String, PushItUser> mRequests;
    private LinkedHashMap<String, PushItUser> mUsers;

    private DatabaseManager mDatabaseManager;

    private RecyclerView mRequestsRecyclerView;
    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;
    private TextView mRequestsTextView;
    private TextView mUsersTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.page_followers);
        setHasOptionsMenu(true);

        mDatabaseManager = DatabaseManager.get(getActivity());

        final UUID id = (UUID) getArguments().getSerializable(PageFragment.EXTRA_ID);

        mDatabaseManager.addPagesListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPage = Page.getPageFollowersFromDB(dataSnapshot.child(id.toString()));

                if(mPage == null)
                    return;

                mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUsers = new LinkedHashMap<String, PushItUser>();
                        mRequests = new LinkedHashMap<String, PushItUser>();

                        for(String userId : mPage.getFollowersIdentifiers().keySet()) {
                            final PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);

                            if(user != null)
                                (mPage.getFollowersIdentifiers().get(userId) ? mUsers : mRequests).put(userId, user);
                        }

                        if(getView() != null)
                            configureAdapter();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_followers, container, false);

        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new FollowersListRecycleViewAdapter(getActivity(), false));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRequestsRecyclerView = v.findViewById(R.id.requestsRecyclerView);
        mRequestsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRequestsRecyclerView.setAdapter(new FollowersListRecycleViewAdapter(getActivity(), true));
        mRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyTextView = v.findViewById(R.id.emptyTextView);
        mUsersTextView = v.findViewById(R.id.usersTextViewLabel);
        mRequestsTextView = v.findViewById(R.id.requestsTextViewLabel);

        if(mUsers != null) {
            configureAdapter();
        }

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                getActivity().finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Fragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(PageFragment.EXTRA_ID, id);

        PageFollowersFragment fragment = new PageFollowersFragment();
        fragment.setArguments(args);

        return fragment;
    }


    private void configureAdapter() {
        final FollowersListRecycleViewAdapter adapter = (FollowersListRecycleViewAdapter) mRecyclerView.getAdapter();
        final FollowersListRecycleViewAdapter requestsAdapter = (FollowersListRecycleViewAdapter) mRequestsRecyclerView.getAdapter();

        adapter.setUsers(mUsers);
        requestsAdapter.setUsers(mRequests);

        adapter.notifyDataSetChanged();
        requestsAdapter.notifyDataSetChanged();

        mEmptyTextView.setVisibility(adapter.getItemCount() + requestsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
                mEmptyTextView.setVisibility(adapter.getItemCount() + requestsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });


        mRequestsRecyclerView.setVisibility(requestsAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
        mRequestsTextView.setVisibility(requestsAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
        mUsersTextView.setVisibility(requestsAdapter.getItemCount() != 0 && adapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);

        requestsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
                mRequestsRecyclerView.setVisibility(requestsAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
                mRequestsTextView.setVisibility(requestsAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
                mUsersTextView.setVisibility(requestsAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
            }
        });
    }


    private class FollowersListRecycleViewAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private LinkedHashMap<String, PushItUser> mUsers;
        private boolean mIsRequests; // true if requests recyclerView, false if users recyclerView

        FollowersListRecycleViewAdapter(Context context, boolean requests) {
            mContext = context;
            mIsRequests = requests;
        }

        public void setUsers(LinkedHashMap<String, PushItUser> users) {
            mUsers = users;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(mIsRequests) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_following_requests_list, parent, false);
                return new RequestViewHolder(v);
            }

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_followers_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Map.Entry<String, PushItUser> userEntry = new ArrayList<Map.Entry<String, PushItUser>>(mUsers.entrySet()).get(position);

            final String userId = userEntry.getKey();
            final PushItUser user = userEntry.getValue();

            ((ViewHolder) holder).mEmailTextView.setText(user.getEmail());
            ((ViewHolder) holder).mStatusTextView.setText((user.getStatus()) ? R.string.status_creator : R.string.status_follower);

            ((ViewHolder) holder).mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user.getStatus()) {
                        UUID pageId = UUID.fromString(user.getPageId());

                        Intent intent = new Intent(mContext, PageActivity.class);
                        intent.putExtra(PageFragment.EXTRA_ID, pageId);
                        mContext.startActivity(intent);
                    }
                }
            });

            if(user.getStatus())
                loadLogoImage(user.getPageId(), ((ViewHolder) holder).mImageView);

            if(mIsRequests) {
                ((RequestViewHolder) holder).mAcceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPage.approveFollower(user, userId);
                        mDatabaseManager.followPage(user, userId, mPage);
                    }
                });

                ((RequestViewHolder) holder).mRejectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPage.getFollowersIdentifiers().remove(userId);
                        mDatabaseManager.updatePageFollowers(mPage);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return (mUsers == null) ? 0 : mUsers.size();
        }

        // Returns the user whose menu item was selected
        private Map.Entry<String, PushItUser> getUser(MenuItem menuItem) {
            if(mUsers == null) return null;

            int position = menuItem.getOrder(); // we put the adapter position instead of the order
            return new ArrayList<>(mUsers.entrySet()).get(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout mLinearLayout;
            TextView mEmailTextView;
            TextView mStatusTextView;
            CircleImageView mImageView;

            ViewHolder(View v) {
                super(v);
                mLinearLayout = v.findViewById(R.id.followersListItem);
                mEmailTextView = v.findViewById(R.id.accountEmail);
                mStatusTextView = v.findViewById(R.id.accountStatus);
                mImageView = v.findViewById(R.id.pageImageView);

                if(!mIsRequests && mPage.isPrivate()) {
                    v.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                            contextMenu.add(0, R.id.context_menu_remove_follower, getAdapterPosition(), R.string.remove_follower);
                        }
                    });
                }
            }
        }

        public class RequestViewHolder extends ViewHolder {
            Button mAcceptButton;
            Button mRejectButton;

            RequestViewHolder(View v) {
                super(v);
                mAcceptButton = v.findViewById(R.id.acceptButon);
                mRejectButton = v.findViewById(R.id.rejectButton);
            }
        }

        private void loadLogoImage(final String pageId, final ImageView imageView) {
            FirebaseStorage.getInstance().getReference("pages").child(pageId).child("logo.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (mContext == null) return;

                    if (task.getException() == null) {
                        Uri logoUri = task.getResult();
                        Glide.with(mContext).load(logoUri).into(imageView);
                    }

                    else {
                        imageView.setImageResource(R.drawable.page_placeholder);
                    }
                }
            });
        }
    }

    // called when context menu item is selected
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.context_menu_remove_follower) {
            Map.Entry<String, PushItUser> userEntry = ((FollowersListRecycleViewAdapter) mRecyclerView.getAdapter()).getUser(menuItem);
            mPage.removeFollower(userEntry.getValue(), userEntry.getKey());
            mDatabaseManager.followPage(userEntry.getValue(), userEntry.getKey(), mPage);
            return true;
        }

        return super.onContextItemSelected(menuItem);
    }
}
