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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PageFollowersFragment extends Fragment {

    private Page mPage;
    private ArrayList<PushItUser> mUsers;

    private DatabaseManager mDatabaseManager;
    private ValueEventListener mDatabaseListener;

    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.page_followers);
        setHasOptionsMenu(true);

        mDatabaseManager = DatabaseManager.get(getActivity());

        final UUID id = (UUID) getArguments().getSerializable(PageFragment.EXTRA_ID);

        mDatabaseListener = mDatabaseManager.addPagesListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPage = Page.getPageFollowersFromDB(dataSnapshot.child(id.toString()));

                mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUsers = new ArrayList<>();

                        for(String uid : mPage.getFollowersIdentifiers()) {
                            final PushItUser user = dataSnapshot.child(uid).getValue(PushItUser.class);

                            if(user != null)
                                mUsers.add(user);
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
        mRecyclerView.setAdapter(new FollowersListRecycleViewAdapter(getActivity()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyTextView = v.findViewById(R.id.emptyTextView);

        if(mUsers != null) {
            configureAdapter();
        }

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                Intent intent = NavUtils.getParentActivityIntent(getActivity());
                intent.putExtra(PageFragment.EXTRA_ID, mPage.getId());

                NavUtils.navigateUpTo(getActivity(), intent);
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

        adapter.setUsers(mUsers);
        adapter.notifyDataSetChanged();

        mEmptyTextView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

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
                mEmptyTextView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }


    private class FollowersListRecycleViewAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private ArrayList<PushItUser> mUsers;

        FollowersListRecycleViewAdapter(Context context) {
            mContext = context;
        }

        public void setUsers(ArrayList<PushItUser> users) {
            mUsers = users;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_followers_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final PushItUser user = mUsers.get(position);

            ((ViewHolder) holder).mEmailTextView.setText(user.getEmail());
            ((ViewHolder) holder).mStatusTextView.setText((user.getStatus()) ? R.string.status_creator : R.string.status_follower);

            ((ViewHolder) holder).mLayout.setOnClickListener(new View.OnClickListener() {
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
        }

        @Override
        public int getItemCount() {
            return (mUsers == null) ? 0 : mUsers.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout mLayout;
            TextView mEmailTextView;
            TextView mStatusTextView;
            CircleImageView mImageView;

            ViewHolder(View v) {
                super(v);
                mLayout = v.findViewById(R.id.followersListItem);
                mEmailTextView = v.findViewById(R.id.accountEmail);
                mStatusTextView = v.findViewById(R.id.accountStatus);
                mImageView = v.findViewById(R.id.pageImageView);
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
                }
            });
        }
    }
}
