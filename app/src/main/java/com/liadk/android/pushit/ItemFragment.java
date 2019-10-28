package com.liadk.android.pushit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    private PushItUser mUser;
    private boolean mIsOwner;

    private FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;
    private ValueEventListener mDatabaseListener;

    private ImageView mImageView;
    private TextView mTitleTextView;
    private TextView mDetailsTextView;
    private TextView mTextView;
    private TextView[] mSegmentTextViews;
    private ImageView[] mImageViews;
    //private SurfaceView[] mSurfaceViews;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("");

        final UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseManager = DatabaseManager.get(getActivity());
        mDatabaseListener = mDatabaseManager.addItemsListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mItem = Item.fromDB(dataSnapshot.child(id.toString()));

                if(mItem != null)
                    updateUI();

                mIsOwner = mItem != null && mUser != null && mUser.getStatus() && mItem.getOwnerId().toString().equals(mUser.getPageId());
                new EventsLogger(getActivity()).log("item_is_owner1", "is_auth_null", mAuth.getCurrentUser() != null, "is_user_null", mUser != null, "user_status", (mUser != null) ? mUser.getStatus(): false, "is_id_equal", (mUser != null && mUser.getStatus()) ? mItem.getOwnerId().toString().equals(mUser.getPageId()) : false, "is_owner", mIsOwner);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser() == null) return;

                String userId = mAuth.getCurrentUser().getUid();
                mUser = dataSnapshot.child(userId).getValue(PushItUser.class);

                mIsOwner = mItem != null && mUser != null && mUser.getStatus() && mItem.getOwnerId().toString().equals(mUser.getPageId());
                new EventsLogger(getActivity()).log("item_is_owner2", "is_auth_null", mAuth.getCurrentUser() != null, "is_user_null", mUser != null, "user_status", (mUser != null) ? mUser.getStatus(): false, "is_id_equal", (mUser != null && mUser.getStatus()) ? mItem.getOwnerId().toString().equals(mUser.getPageId()) : false, "is_owner", mIsOwner);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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
        //mSurfaceViews = new SurfaceView[] { (SurfaceView) v.findViewById(R.id.itemSurfaceView1), (SurfaceView) v.findViewById(R.id.itemSurfaceView2) };

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
        mDatabaseManager.removeItemsListener(mDatabaseListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mDatabaseListener != null)
            mDatabaseManager.removeItemsListener(mDatabaseListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_item, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        new EventsLogger(getActivity()).log("item_options_menu", "is_owner", mIsOwner);
        for(int i = 0; i < menu.size(); i++)
            menu.getItem(i).setVisible(mIsOwner);
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

                            StorageManager storageManager = StorageManager.get(getActivity());
                            storageManager.deleteItem(mItem);

                            mDatabaseManager.deleteItem(mItem);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            alertDialog.show();
            return true;
        }

        else if(item.getItemId() == android.R.id.home) {

            if (NavUtils.getParentActivityName(getActivity()) != null && mItem != null) {
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
            mSegmentTextViews[i].setVisibility(View.GONE);

            if(i < mItem.getSegmentsCounter()) {
                mSegmentTextViews[i].setVisibility(View.VISIBLE);
                mSegmentTextViews[i].setText(mItem.getTextSegments().get(i+1));

                mImageViews[i].setVisibility(View.VISIBLE);

                final int index = i;
                final String filename = "image" + i + ".png";

                FirebaseStorage.getInstance().getReference("items").child(mItem.getId().toString()).child(filename).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(getActivity() == null) return;

                        if (task.getException() == null && index < mItem.getSegmentsCounter()) {
                            Glide.with(getActivity()).load(task.getResult()).into(mImageViews[index]);
                        }
                    }
                });
            }
        }
    }

    private void loadMainImage(final ImageView imageView) {
        FirebaseStorage.getInstance().getReference("items").child(mItem.getId().toString()).child("image.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(getActivity() == null) return;

                if(task.getException() == null) {
                    Glide.with(getActivity()).load(task.getResult()).into(imageView);
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
