package com.liadk.android.pushit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class ItemFragment extends Fragment {

    public static final String EXTRA_ID = "itemId";
    private static final int REQUEST_EDIT = 0;

    private Item mItem;
    private DatabaseReference mItemsDatabase;
    private ValueEventListener mValueEventListener;

    ImageView mImageView;
    TextView mTitleTextView;
    TextView mDetailsTextView;
    TextView mTextView;
    TextView[] mSegmentTextViews;
    ImageView[] mImageViews;
    SurfaceView[] mSurfaceViews;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("");

        final UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);

        mItemsDatabase = FirebaseDatabase.getInstance().getReference("items");
        mValueEventListener = mItemsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mItem = Item.fromDB(dataSnapshot.child(id.toString()));
                if(mItem != null)
                    updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_item, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mImageView = (ImageView) v.findViewById(R.id.mainImageView);
        mTitleTextView = (TextView) v.findViewById(R.id.titleTextView);
        mDetailsTextView = (TextView) v.findViewById(R.id.detailsTextView);
        mTextView = (TextView) v.findViewById(R.id.mainTextView);
        mSegmentTextViews = new TextView[] { (TextView) v.findViewById(R.id.seg1TextView), (TextView) v.findViewById(R.id.seg2TextView) };
        mImageViews = new ImageView[] { (ImageView) v.findViewById(R.id.itemImageView1), (ImageView) v.findViewById(R.id.itemImageView2) };
        mSurfaceViews = new SurfaceView[] { (SurfaceView) v.findViewById(R.id.itemSurfaceView1), (SurfaceView) v.findViewById(R.id.itemSurfaceView2) };

        if(mItem != null)
            updateUI();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mItem != null) updateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mItemsDatabase.removeEventListener(mValueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mValueEventListener != null)
            mItemsDatabase.removeEventListener(mValueEventListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_edit_item) {
            Intent intent = new Intent(getActivity(), EditItemActivity.class);
            intent.putExtra(EXTRA_ID, mItem.getId());
            startActivityForResult(intent, REQUEST_EDIT);
            return true;
        }

        else if(item.getItemId() == R.id.menu_item_delete_item) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete_article)
                    .setMessage(R.string.delete_article_dialog)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();

                            DatabaseReference pageItemsDatabase = FirebaseDatabase.getInstance().getReference("pages/" + mItem.getOwnerId().toString() + "/items");
                            StorageManager storageManager = StorageManager.get(getActivity());

                            mItemsDatabase.child(mItem.getId().toString()).removeValue();
                            pageItemsDatabase.child(mItem.getId().toString()).removeValue();

                            storageManager.deleteItem(mItem);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            alertDialog.show();
            return true;
        }

        else if(item.getItemId() == android.R.id.home) {

            if (NavUtils.getParentActivityName(getActivity()) != null) {
                Intent intent = NavUtils.getParentActivityIntent(getActivity());
                intent.putExtra(PageFragment.EXTRA_ID, mItem.getOwnerId());

                NavUtils.navigateUpTo(getActivity(), intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_CANCELED) {
            getActivity().finish();
        }
    }

    public void updateUI() {
        loadMainImage(mImageView);
        mTitleTextView.setText(mItem.getTitle());
        mDetailsTextView.setText(mItem.getDetails());
        mTextView.setText(mItem.getText());

        for(int i = 0; i < EditItemFragment.MAX_MEDIA; i++) {

            mImageViews[i].setVisibility(View.GONE);
            mSurfaceViews[i].setVisibility(View.GONE);
            mSegmentTextViews[i].setVisibility(View.GONE);

            if(i < mItem.getSegmentsCounter()) {
                Object mediaSegment = mItem.getMediaSegments().get(i);

                if (mediaSegment instanceof MediaStore.Video) {
                    mSurfaceViews[i].setVisibility(View.VISIBLE);
                    if(mediaSegment != null) {
                        // TODO add Video
                    }
                }
                else if (mediaSegment instanceof Bitmap) { // TODO check if "instanceOf bitmap" check needs to be done on the second if similarly to EditItemFragment
                    mImageViews[i].setVisibility(View.VISIBLE);
                    if(mediaSegment != null) {
                        mImageViews[i].setImageBitmap((Bitmap) mediaSegment);
                    }
                }
                mSegmentTextViews[i].setVisibility(View.VISIBLE);
                mSegmentTextViews[i].setText(mItem.getTextSegments().get(i+1));
            }
        }
    }

    private void loadMainImage(final ImageView imageView) {
        FirebaseStorage.getInstance().getReference("items").child(mItem.getId().toString()).child("image.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.getException() == null) {
                    mItem.setImageUri(task.getResult());
                    Glide.with(getActivity()).load(mItem.getImageUri()).into(imageView);
                }
            }
        });
    }

    public static ItemFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ItemFragment.EXTRA_ID, id);

        ItemFragment fragment = new ItemFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
