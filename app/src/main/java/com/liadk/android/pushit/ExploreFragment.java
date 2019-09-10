package com.liadk.android.pushit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.UUID;

public class ExploreFragment extends Fragment {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mPagesDatabase;
    private ValueEventListener mValueEventListener;

    private ArrayList<Page> mPages;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.explore);
        mDatabase = FirebaseDatabase.getInstance();

        mPagesDatabase = FirebaseDatabase.getInstance().getReference("pages");
        mValueEventListener = mPagesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPages = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    mPages.add(Page.getPageDetailsFromDB(ds));
                }

                if(mRecyclerView != null)
                    mRecyclerView.getAdapter().notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        //resetDatabase();
        //addDataToDatabase();
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

        for(Page page : PageCollection.get(getActivity()).getPages()) {
            page.pushToDB(pagesDatabase);
            //page.uploadLogoImage(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.bibi_face); TODO TAKE CARE OF IMAGES UPLOAD
        }

        for(Item item : ItemCollection.get(getActivity()).getItems()) {
            item.pushToDB(itemsDatabase);
            //item.uploadImage(item.getImage());  TODO TAKE CARE OF IMAGES UPLOAD
        }
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

    @Override
    public void onDetach() {
        super.onDetach();
        mPagesDatabase.removeEventListener(mValueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mValueEventListener != null)
            mPagesDatabase.removeEventListener(mValueEventListener);
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

        PageListRecycleViewAdapter(Context context) {
            mContext = context;
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

            loadLogoImage(page, ((ViewHolder)holder).mImageView);
        }

        @Override
        public int getItemCount() {
            if(mPages == null) return 0;
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

    private void loadLogoImage(final Page page, final ImageView imageView) {
        FirebaseStorage.getInstance().getReference("pages").child(page.getId().toString()).child("logo.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(getActivity() == null) return;

                if(task.getException() == null) {
                    page.setLogoImageUrl(task.getResult());
                    Glide.with(getActivity()).load(page.getLogoImageUrl()).into(imageView);
                }
            }
        });
    }
}
