package com.liadk.android.pushit;

import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class PageLogoFragment extends Fragment {

    private Page mPage;
    private DatabaseReference mPagesDatabase;
    private StorageManager mStorageManager;

    private ImageView mImageView;
    private ImageButton mImageButton;
    private TextView mNoLogoTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.set_page_logo);

        mStorageManager = StorageManager.get(getActivity());

        final UUID id = (UUID) getArguments().getSerializable(PageFragment.EXTRA_ID);

        mPagesDatabase = FirebaseDatabase.getInstance().getReference("pages");
        mPagesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mPage = Page.getPageSettingsFromDB(dataSnapshot.child(id.toString()));
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
                        .setTitle(getString(R.string.set_page_logo))
                        .setSystemDialog(true))
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                if(r.getError() == null)
                                    launchCrop(r.getUri());
                            }
                        }).show(fm);
            }
        });
    }

    private void launchCrop(Uri uri) {
        String filename = uri.getLastPathSegment();
        Uri destUri = Uri.fromFile(new File(getActivity().getCacheDir(), filename));

        UCrop.of(uri, destUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(200, 200)
                .start(getActivity(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            mStorageManager.uploadPageLogoImage(mPage, resultUri); // upload image to storage
            onImageUpdated(resultUri);
        }

        else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    private void onImageUpdated(Uri localImageUri) {
        if(localImageUri != null)
            mImageView.setImageURI(localImageUri);

        else
            onImageUpdated();
    }

    private void onImageUpdated() {
        FirebaseStorage.getInstance().getReference("pages").child(mPage.getId().toString()).child("logo.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
            if(task.getException() == null) {
                mNoLogoTextView.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);

                mPage.setLogoImageUrl(task.getResult());
                Glide.with(getActivity()).load(mPage.getLogoImageUrl()).into(mImageView);
            }

            else {
                mImageView.setVisibility(View.GONE);
                mNoLogoTextView.setVisibility(View.VISIBLE);
            }
            }
        });
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
