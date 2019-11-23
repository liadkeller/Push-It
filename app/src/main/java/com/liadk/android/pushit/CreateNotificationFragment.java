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
    private Page mOwner;
    private String mEditTextTitle = "";
    private boolean mSame = false;

    private DatabaseManager mDatabaseManager;
    private ValueEventListener mDatabaseListener;
    StorageManager mStorageManager;

    private EditText mTitleEditText;
    private CheckBox mTitleCheckBox;
    private ImageView mImageView;

    private Button mImageButton;
    private Button mPublishButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.create_notification);
        ((CreateNotificationActivity) getActivity()).setOnBackPressedListener(this);  // this class has a "onBackPressed()" method as needed
        setHasOptionsMenu(true);

        mDatabaseManager = DatabaseManager.get(getActivity());
        mStorageManager = StorageManager.get(getActivity());

        final UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);

        mDatabaseListener = mDatabaseManager.addDatabaseListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                mItem = Item.fromDB(dataSnapshot.child("items").child(id.toString()));
                mOwner = Page.fromDB(dataSnapshot.child("pages").child(mItem.getOwnerId().toString()));

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
        mTitleEditText = v.findViewById(R.id.pushTitleEditText);
        mTitleCheckBox = v.findViewById(R.id.pushTitleCheckBox);
        mImageView = v.findViewById(R.id.pushImageView);
        mImageButton = v.findViewById(R.id.pushImageButton);
        mPublishButton = v.findViewById(R.id.publishButton);

        // Configuring
        if(mItem != null)
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
                    mEditTextTitle = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mTitleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSame = isChecked;
                mTitleEditText.setEnabled(!isChecked); // if same - edit text should be disabled
                mTitleEditText.setText(isChecked ? mItem.getTitle() : mEditTextTitle);
            }
        });

        mPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((mSame) ? mItem.getTitle() : mEditTextTitle).equals("")) {
                    Toast.makeText(getActivity(), R.string.no_notification_title_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.publish_article)
                        .setMessage(R.string.publish_notification_dialog)
                        .setPositiveButton(R.string.publish, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mItem.getState() == Item.State.NEW)
                                    updateState();

                                else
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
                                new EventsLogger(getActivity()).log("notification_image_taken", "error", r.getError() != null ? r.getError().toString() : "No Error");
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
                .withMaxResultSize(1000, 625)
                .start(getActivity(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            mItem.setImageUri(resultUri);
            onImageUpdated();
        }

        else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    private void onImageUpdated() {
        if(mItem.getImageUri() != null)
            mImageView.setImageURI(mItem.getImageUri());

        else {
            FirebaseStorage.getInstance().getReference("items").child(mItem.getId().toString()).child("notification-image.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (getActivity() == null) return;

                    if (task.getException() == null) {
                        Glide.with(getActivity()).load(task.getResult()).into(mImageView);
                    } else {
                        FirebaseStorage.getInstance().getReference("items").child(mItem.getId().toString()).child("image.png").getDownloadUrl().addOnCompleteListener(this);
                    }
                }
            });
        }
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

    // updates item state to PUBLISHED
    private void updateState() {
        // updateState(PUBLISHED) is here
        Item.State oldState = mItem.getState();
        mItem.setState(Item.State.PUBLISHED);
        mItem.setPublishTime();
        saveChanges(oldState);
        Toast.makeText(getActivity(), R.string.publish_article_toast, Toast.LENGTH_LONG).show();

        // exits two layers
        Intent intent = new Intent(getActivity(), ItemActivity.class);
        intent.putExtra(ItemFragment.EXTRA_ID, mItem.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        getActivity().finish();
        startActivity(intent);
    }

    private void saveChanges(Item.State oldState) {
        if(oldState == Item.State.NEW)              // never saved before
            mDatabaseManager.addItemToPage(mItem);

        mDatabaseManager.updateItemPublished(mItem);
        mStorageManager.uploadNotificationImage(mItem); // Saves Notification Image

        PushNotification notification = new PushNotification(mItem, mOwner, mEditTextTitle, mSame);
        mDatabaseManager.pushNotificationToDB(notification);
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
        mDatabaseManager.removeItemsListener(mDatabaseListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mDatabaseListener != null)
            mDatabaseManager.removeItemsListener(mDatabaseListener);
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
