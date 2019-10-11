package com.liadk.android.pushit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FollowFragment extends Fragment {

    protected FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;

    private ArrayList<Page> mPages;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private LinearLayout mNoUserView;
    private TextView mEmptyTextView;
    private Button mLoginButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.follow);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseManager = DatabaseManager.get(getActivity());

        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null) return;

        final String userId = user.getUid();

        mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);

                if(user == null) return;

                mDatabaseManager.addPagesListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mPages = new ArrayList<>();
                        for(String pageId : user.getFollowedPages())
                            mPages.add(Page.getPageDetailsFromDB(dataSnapshot.child(pageId)));

                        if(mRecyclerView != null) {
                            configureAdapter();
                        }
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
        View v = inflater.inflate(R.layout.fragment_follow, container, false);

        mProgressBar = v.findViewById(R.id.progressBar);

        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new PageListRecycleViewAdapter(getActivity(), PageListRecycleViewAdapter.PAGES_FOLLOW));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyTextView = v.findViewById(R.id.emptyTextView);
        mNoUserView = v.findViewById(R.id.noUserView);
        mLoginButton = v.findViewById(R.id.loginButton);

        if(mAuth.getCurrentUser() == null) {
            mNoUserView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        if(mPages != null) {
            configureAdapter();
        }
        
        return v;
    }

    private void configureAdapter() {
        final PageListRecycleViewAdapter adapter = (PageListRecycleViewAdapter) mRecyclerView.getAdapter();

        mNoUserView.setVisibility(View.GONE);
        adapter.setPages(mPages);
        mProgressBar.setVisibility(View.GONE);
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
}