package com.liadk.android.pushit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.UUID;

public class PageLogoFragment extends Fragment {

    private Page mPage;
    private DatabaseReference mPagesDatabase;

    private ImageView mImageView;
    private ImageButton mImageButton;
    private TextView mNoLogoTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.set_page_logo);

        final UUID id = (UUID) getArguments().getSerializable(PageFragment.EXTRA_ID);

        mPagesDatabase = FirebaseDatabase.getInstance().getReference("pages");
        mPagesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPage = Page.fromDB(dataSnapshot.child(id.toString()));
                if(getView() != null) {
                    configureImageButtonListener();
                    onImageUpdated();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_logo, container, false);

        // Referencing Widgets
        mImageView = (ImageView) v.findViewById(R.id.pageLogoImageView);
        mImageButton = (ImageButton) v.findViewById(R.id.pageLogoButton);
        mNoLogoTextView = (TextView) v.findViewById(R.id.noLogoTextView);

        // Configuring
        if(mPage != null) {
            configureImageButtonListener();
            onImageUpdated();
        }

        return v;
    }

    private void configureImageButtonListener() {
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                PickImageDialog.build(new PickSetup()
                        .setTitle(getString(R.string.set_push_image))
                        .setSystemDialog(true))
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                if(r.getError() == null) {
                                    mPage.setLogoImage(r.getBitmap());
                                    onImageUpdated();
                                }
                            }
                        }).show(fm);
            }
        });
    }

    private void onImageUpdated() {
        if(mPage.getLogoImage() == null) {
            mImageView.setVisibility(View.GONE);
            mNoLogoTextView.setVisibility(View.VISIBLE);
        }
        else {
            mNoLogoTextView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(mPage.getLogoImage());
        }
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

        PageLogoFragment fragment = new PageLogoFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
