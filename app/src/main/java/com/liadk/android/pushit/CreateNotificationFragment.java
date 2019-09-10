package com.liadk.android.pushit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class CreateNotificationFragment extends Fragment implements EditItemActivity.OnBackPressedListener {

    private Item mItem;
    private PushNotification mNotification;
    private Uri mLocalImageUri;

    private ValueEventListener mValueEventListener;
    private DatabaseReference mItemsDatabase;
    StorageManager mStorageManager;

    private EditText mTitleEditText;
    private CheckBox mTitleCheckBox;
    private ImageView mImageView;

    private Button mImageButton;
    private Button mPublishButton;


    private class PushNotification {
        private UUID mId;
        private String mEditTextTitle = new String();
        private String mItemTitle;
        private boolean mSame = false;
        private Uri mImageUrl;

        public PushNotification(Item item) {
            mId = item.getId();
            mItemTitle = item.getTitle();
            mImageUrl = item.getImageUri();
        }

        public void setEditTextTitle(String mTitle) {
            this.mEditTextTitle = mTitle;
        }

        public void setSame(boolean mSame) {
            this.mSame = mSame;
        }

        public void setImageUrl(Uri imageUrl) {
            this.mImageUrl = imageUrl;
        }

        public UUID getId() {
            return mId;
        }

        public String getEditTextTitle() {
            return mEditTextTitle;
        }

        public String getItemTitle() {
            return mItemTitle;
        }

        public boolean isSame() {
            return mSame;
        }

        public Uri getImageUrl() {
            return mImageUrl;
        }

        public String getNotificationTitle() {
            return (mSame) ? mItemTitle : mEditTextTitle;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.create_notification);
        ((CreateNotificationActivity) getActivity()).setOnBackPressedListener((EditItemActivity.OnBackPressedListener) this);  // this class has a "onBackPressed()" method as needed
        setHasOptionsMenu(true);

        mStorageManager = StorageManager.get(getActivity());

        final UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);

        mItemsDatabase = FirebaseDatabase.getInstance().getReference("items");
        mValueEventListener = mItemsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mItem = Item.fromDB(dataSnapshot.child(id.toString()));
                mNotification = new PushNotification(mItem);

                if(getView() != null)
                    configureView(getView());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_notification, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Widgets
        mTitleEditText = (EditText) v.findViewById(R.id.pushTitleEditText);
        mTitleCheckBox = (CheckBox) v.findViewById(R.id.pushTitleCheckBox);
        mImageView = (ImageView) v.findViewById(R.id.pushImageView);
        mImageButton = (Button) v.findViewById(R.id.pushImageButton);
        mPublishButton = (Button) v.findViewById(R.id.publishButton);

        // Configuring
        if(mNotification != null)
            configureView(v);

        return v;
    }

    private void configureView(View v) {
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mTitleEditText.isEnabled())
                    mNotification.setEditTextTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mTitleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mTitleEditText.setEnabled(false);
                    mTitleEditText.setText(mNotification.getItemTitle());
                    mNotification.setSame(true);
                }

                else {
                    mTitleEditText.setEnabled(true);
                    mTitleEditText.setText(mNotification.getEditTextTitle());
                    mNotification.setSame(false);
                }
            }
        });

        mPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNotification.getNotificationTitle().equals("")) {
                    Toast.makeText(getActivity(), R.string.no_notification_title_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.publish_article)
                        .setMessage(R.string.publish_notification_dialog)
                        .setPositiveButton(R.string.publish, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                                // TODO Perform and Launch Push Notification !!!


                                showTimeDialog(); // The dialog will lead to setting the state as PUBLISHED
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();

                alertDialog.show();
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                PickImageDialog.build(new PickSetup()
                        .setTitle(getString(R.string.set_push_image))
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

        onImageUpdated();
    }

    private void launchCrop(Uri uri) {
        String filename = uri.getLastPathSegment();
        Uri destUri = Uri.fromFile(new File(getActivity().getCacheDir(), filename));

        UCrop.of(uri, destUri)
                .withAspectRatio(8, 5)
                .withMaxResultSize(400, 250)
                .start(getActivity(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            mItem.setImageUri(resultUri);
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


        FirebaseStorage.getInstance().getReference("items").child(mNotification.getId().toString()).child("notification-image.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(getActivity() == null) return;

                if(task.getException() == null) {
                    mNotification.setImageUrl(task.getResult());
                    Glide.with(getActivity()).load(mNotification.getImageUrl()).into(mImageView);
                }

                else {
                    FirebaseStorage.getInstance().getReference("items").child(mNotification.getId().toString()).child("image.png").getDownloadUrl().addOnCompleteListener(this);
                }
            }
        });
    }

    private void showTimeDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.set_time)
                .setMessage(R.string.publication_time_dialog_state_changed)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mItem.setCurrentTime();
                        updateState();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateState();
                    }
                })
                .create();

        alertDialog.show();
    }

    private void updateState() {
        // updateState(PUBLISHED) is here
        mItem.setState(Item.State.PUBLISHED);
        saveChanges();
        Toast.makeText(getActivity(), R.string.publish_article_toast, Toast.LENGTH_SHORT).show();

        // exits two layers
        Intent intent = new Intent(getActivity(), ItemActivity.class);
        intent.putExtra(ItemFragment.EXTRA_ID, mItem.getId());
        NavUtils.navigateUpTo(getActivity(), intent);
    }

    private void saveChanges() {
        mItemsDatabase.child(mItem.getId().toString()).child("state").setValue(mItem.getState().toString());
        mStorageManager.uploadNotificationImage(mItem); // Saves Notification Image

        // TODO save Notification on DB
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    // The onBackPressed of the OnBackPressedListener Interface
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.exit)
                .setMessage(R.string.exit_notification_dialog)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exit();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();

        alertDialog.show();
    }


    public void exit() {
        if (NavUtils.getParentActivityName(getActivity()) != null) {
            Intent intent = NavUtils.getParentActivityIntent(getActivity());
            intent.putExtra(ItemFragment.EXTRA_ID, mItem.getId());

            NavUtils.navigateUpTo(getActivity(), intent);
        }
    }

    public static Fragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ItemFragment.EXTRA_ID, id);

        CreateNotificationFragment fragment = new CreateNotificationFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
