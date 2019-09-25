package com.liadk.android.pushit;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ExploreFragment extends Fragment {

    private DatabaseManager mDatabaseManager;
    private ValueEventListener mDatabaseListener;

    private ArrayList<Page> mPages;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.explore);

        mDatabaseManager = DatabaseManager.get(getActivity());
        mDatabaseListener = mDatabaseManager.addPagesListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPages = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    mPages.add(Page.getPageDetailsFromDB(ds));
                }

                if(mRecyclerView != null) {
                    ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).setPages(mPages);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new PageListRecycleViewAdapter(getActivity(), PageListRecycleViewAdapter.PAGES_EXPLORE));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(mPages != null) {
            ((PageListRecycleViewAdapter) mRecyclerView.getAdapter()).setPages(mPages);
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
        
        return v;
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
}
