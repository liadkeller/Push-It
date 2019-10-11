package com.liadk.android.pushit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private PushItUser mUser;
    private String mUserId;

    private final int mScreen;
    private int mLayoutResource;

    PageListRecycleViewAdapter(Context context, int screen) {
        mContext = context;
        mScreen = screen;
        mLayoutResource = (screen == PAGES_FOLLOW) ? R.layout.layout_item_follow_page_list : R.layout.layout_item_explore_page_list;
    }

    public void setPages(ArrayList<Page> pages) {
        mPages = pages;
    }

    public void setUser(PushItUser mUser) {
        this.mUser = mUser;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public PushItUser getUser() {
        return mUser;
    }

    public String getUserId() {
        return mUserId;
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

        if (page.getDescription() == null || page.getDescription().isEmpty())
            ((ViewHolder) holder).mDescTextView.setVisibility(View.GONE);

        else {
            ((ViewHolder) holder).mDescTextView.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).mDescTextView.setText(page.getDescription());
        }

        ((ViewHolder) holder).mLinearLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (hasAccess(page)) { // if Explore has access or if Follow
                   Intent intent = new Intent(mContext, PageActivity.class);
                   intent.putExtra(PageFragment.EXTRA_ID, page.getId());
                   mContext.startActivity(intent);
               }

               else if(mUser == null)
                   Toast.makeText(mContext, R.string.private_page_click_no_user, Toast.LENGTH_LONG).show();

               else
                   Toast.makeText(mContext, R.string.private_page_click, Toast.LENGTH_LONG).show();
           }
        });

        loadLogoImage(page, ((ViewHolder) holder).mImageView);
    }

    private boolean hasAccess(Page page) {
        return page.isPublic() || (mUser != null && mUser.isFollowing(page)) || mScreen == PAGES_FOLLOW;
    }

    @Override
    public int getItemCount() {
        return (mPages == null) ? 0 : mPages.size();
    }

    public Page getPage(MenuItem menuItem) {
        if(mPages == null) return null;

        int position = menuItem.getOrder(); // we put the adapter position instead of the order
        return mPages.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mLinearLayout;
        ImageView mImageView;
        TextView mNameTextView;
        TextView mDescTextView;

        ViewHolder(View v) {
            super(v);
            mLinearLayout = v.findViewById(R.id.pageListItemLayout);
            mImageView = v.findViewById(R.id.pageImageView);
            mNameTextView = v.findViewById(R.id.pageNameTextView);
            mDescTextView = v.findViewById(R.id.pageDescTextView);

            v.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    final Page page = mPages.get(getAdapterPosition());

                    if(!hasAccess(page) && mUser != null)
                        contextMenu.add(0, R.id.context_menu_follow_page, getAdapterPosition(), R.string.follow_page);
                }
            });
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
