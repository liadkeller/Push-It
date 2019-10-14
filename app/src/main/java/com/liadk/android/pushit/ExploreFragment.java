package com.liadk.android.pushit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ExploreFragment extends PageListFragment {

    private FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;
    private ValueEventListener mDatabaseListener;

    private ArrayList<Page> mPages;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.explore);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseManager = DatabaseManager.get(getActivity());
        mDatabaseListener = mDatabaseManager.addPagesListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPages = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    Page page = Page.getPageDetailsFromDB(ds);
                    if(page != null)
                        mPages.add(page);
                }

                if(mRecyclerView != null) {
                    configureAdapter(mPages);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser() == null) return;

                String userId = mAuth.getCurrentUser().getUid();
                PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);

                ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).setUser(user);
                ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).setUserId(userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mProgressBar = v.findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new PageListRecycleViewAdapter(getActivity(), PageListRecycleViewAdapter.PAGES_EXPLORE));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(mPages != null) {
            configureAdapter(mPages);
        }
        
        return v;
    }

    private void configureAdapter(ArrayList<Page> pages) {
        ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).setPages(pages);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDatabaseManager.removePagesListener(mDatabaseListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mDatabaseListener != null)
            mDatabaseManager.removePagesListener(mDatabaseListener);
    }

    // called when context menu item is selected
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.context_menu_follow_page) {
            Page page = ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).getPage(menuItem);

            PushItUser user = ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).getUser();
            String userId = ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).getUserId();

            if(mDatabaseManager.followPage(user, userId, page)) {
                page.addNewFollower(user, userId);
                mDatabaseManager.followPage(user, userId, page);

                Toast.makeText(getActivity(), R.string.follow_request_sent, Toast.LENGTH_SHORT).show();
                return true;
            }

            else {
                Toast.makeText(getActivity(), R.string.follow_request_failed, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onContextItemSelected(menuItem);
    }

    public void loadQuery(final String query) {
        mDatabaseManager.addPagesListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                if(query == null) {
                    configureAdapter(mPages);
                    return;
                }

                ArrayList<Page> searchedPages = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    Page page = Page.getPageDetailsFromDB(ds);
                    if(page != null && page.getName() != null && page.getName().toLowerCase().contains(query.toLowerCase()))
                        searchedPages.add(page);
                }

                if(mRecyclerView != null) {
                    if(searchedPages.isEmpty()) {
                        Toast.makeText(getActivity(), R.string.no_pages_found, Toast.LENGTH_SHORT).show();
                        configureAdapter(mPages);
                    }

                    else
                        configureAdapter(searchedPages);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
