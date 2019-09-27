package com.liadk.android.pushit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class PageListRecycleViewAdapter extends RecyclerView.Adapter {

    final static int PAGES_EXPLORE = 0;
    final static int PAGES_FOLLOW = 1;

    private Context mContext;
    private ArrayList<Page> mPages;
    private int mLayoutResource;

    PageListRecycleViewAdapter(Context context, int layout) {
        mContext = context;
        mLayoutResource = (layout == PAGES_FOLLOW) ? R.layout.layout_item_follow_page_list : R.layout.layout_item_explore_page_list;
    }

    public void setPages(ArrayList<Page> pages) {
        mPages = pages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mLayoutResource, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Page page = mPages.get(position);

        ((ViewHolder) holder).mNameTextView.setText(page.getName());

        if (page.getDescription().isEmpty())
            ((ViewHolder) holder).mDescTextView.setVisibility(View.GONE);

        else {
            ((ViewHolder) holder).mDescTextView.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).mDescTextView.setText(page.getDescription());
        }

        ((ViewHolder) holder).mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PageActivity.class);
                intent.putExtra(PageFragment.EXTRA_ID, page.getId());
                mContext.startActivity(intent);
            }
        });

        loadLogoImage(page, ((ViewHolder) holder).mImageView);
    }

    @Override
    public int getItemCount() {
        return (mPages == null) ? 0 : mPages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewGroup mLayout;
        ImageView mImageView;
        TextView mNameTextView;
        TextView mDescTextView;

        ViewHolder(View v) {
            super(v);
            mLayout = v.findViewById(R.id.pageListItemLayout);
            mImageView = v.findViewById(R.id.pageImageView);
            mNameTextView = v.findViewById(R.id.pageNameTextView);
            mDescTextView = v.findViewById(R.id.pageDescTextView);
        }
    }

    private void loadLogoImage(final Page page, final ImageView imageView) {
        FirebaseStorage.getInstance().getReference("pages").child(page.getId().toString()).child("logo.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
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
