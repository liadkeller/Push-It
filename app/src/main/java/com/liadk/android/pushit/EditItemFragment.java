package com.liadk.android.pushit;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.liadk.android.pushit.Item.State.CREATED;
import static com.liadk.android.pushit.Item.State.DRAFT;
import static com.liadk.android.pushit.Item.State.NEW;
import static com.liadk.android.pushit.Item.State.PUBLISHED;

/**
 * Created by user on 10/08/2019.
 */
public class EditItemFragment extends Fragment implements EditItemActivity.OnBackPressedListener {
    private static final String TAG = "EditItemFragment";
    private static final int MAIN_IMAGE_REQUEST = 2; // 0, 1 are MEDIA_REQUEST indexes


    private Item mItem;

    private DatabaseManager mDatabaseManager;
    private ValueEventListener mDatabaseListener;
    private StorageManager mStorageManager;

    private boolean mRecentlySaved = true;


    private EditText titleEditText;
    private EditText authorEditText;
    private EditText mainEditText;

    private Button setImageButton;
    private Button removeImageButton;
    private ImageView imageView;

    private Button setTimeButton;
    private TextView timeTextView;


    protected final static int MAX_MEDIA = 2; // maximum media segments

    private EditText[] editTexts;

    private Button[] setImageButtons;
    private Button[] removeImageButtons;
    private ImageView[] imageImageViews;
    private LinearLayout[] imageLinearLayouts;
    private Button addPhotoButton;

    private LinearLayout saveChangesLayout;
    private Button confirmChangesButton;

    private TextView publishedTextView;
    private Button publishButton;
    private Button createButton;
    private Button draftButton;
    private Button deleteButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setHasOptionsMenu(true);
        ((EditItemActivity) getActivity()).setOnBackPressedListener(this);

        mDatabaseManager = DatabaseManager.get(getActivity());
        mStorageManager = StorageManager.get(getActivity());

        final UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);

        mDatabaseListener = mDatabaseManager.addItemsListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;

                mItem = Item.fromDB(dataSnapshot.child(id.toString()));

                if(mItem != null && getView() != null)
                    configureView(getView());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_item, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Referencing Widgets
        titleEditText = v.findViewById(R.id.titleEditText);
        authorEditText = v.findViewById(R.id.authorEditText);
        mainEditText = v.findViewById(R.id.mainItemEditText);

        setImageButton = v.findViewById(R.id.setImageButton);
        removeImageButton = v.findViewById(R.id.removeImageButton);
        imageView = v.findViewById(R.id.itemImageView);

        setTimeButton = v.findViewById(R.id.setTimeButton);
        timeTextView = v.findViewById(R.id.cardTime);

        editTexts = new EditText[]{v.findViewById(R.id.seg1EditText), v.findViewById(R.id.seg2EditText)};

        setImageButtons = new Button[]{v.findViewById(R.id.setImage1Button), v.findViewById(R.id.setImage2Button)};
        removeImageButtons = new Button[]{v.findViewById(R.id.removeImage1Button), v.findViewById(R.id.removeImage2Button)};
        imageImageViews = new ImageView[]{v.findViewById(R.id.image1ImageView), v.findViewById(R.id.image2ImageView)};
        imageLinearLayouts = new LinearLayout[]{v.findViewById(R.id.image1LinearLayout), v.findViewById(R.id.image2LinearLayout)};
        addPhotoButton = v.findViewById(R.id.addPhotoButton);

        saveChangesLayout = v.findViewById(R.id.saveChangesLayout);
        confirmChangesButton = v.findViewById(R.id.confirmChangesButton);

        publishedTextView = v.findViewById(R.id.publishedTextView);
        publishButton = v.findViewById(R.id.publishButton);
        createButton = v.findViewById(R.id.createButton);
        draftButton = v.findViewById(R.id.draftButton);
        deleteButton = v.findViewById(R.id.deleteButton);

        if(mItem != null)
            configureView(v);

        return v;
    }

    private void configureView(View v) {

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mItem.setTitle(s.toString());
                mRecentlySaved = false;
                new EventsLogger(getActivity()).log("edit_item_title_changed", "new_title", s.toString(), "item_title", mItem.getTitle());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        authorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mItem.setAuthor(s.toString());
                mRecentlySaved = false;
                new EventsLogger(getActivity()).log("edit_item_author_changed", "new_author", s.toString(), "item_author", mItem.getAuthor());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mainEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mItem.setText(s.toString());
                mRecentlySaved = false;
                new EventsLogger(getActivity()).log("edit_item_text_changed", "new_text", s.toString(), "item_text", mItem.getText());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                PickImageDialog.build(new PickSetup()
                        .setTitle(getString(R.string.set_image))
                        .setSystemDialog(true))
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                new EventsLogger(getActivity()).log("main_item_image_taken", "error", r.getError() != null ? r.getError().toString() : "No Error");
                                if(r.getError() == null)
                                    launchCrop(r.getUri(), MAIN_IMAGE_REQUEST);
                            }
                        })
                        .show(fm);
            }
        });

        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOnClickDialog(R.string.remove_image, R.string.delete, R.string.remove_image_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mItem.setHasImage(false);
                        mStorageManager.deleteItemIImage(mItem, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mDatabaseManager.refreshItemImage(mItem); // updates image status (no image)

                                // mRecentlySaved should not be modified as this button causes this change immediately and not after saving
                                boolean recentlySaved = mRecentlySaved;
                                onImageUpdated();
                                mRecentlySaved = recentlySaved;
                            }
                        });
                    }
                });
            }
        });

        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.set_time)
                        .setMessage(getString(R.string.publication_time_dialog_button_clicked, mItem.getTime(), mItem.getFormattedOriginalTime()))
                        .setPositiveButton(R.string.set_current_time, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mItem.setCurrentTime();
                                onTimeUpdated();
                            }
                        })
                        .setNegativeButton(R.string.set_original_time, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mItem.setTime(mItem.getOriginalTime());
                                onTimeUpdated();
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .create();

                alertDialog.show();
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int counter = mItem.getSegmentsCounter();
                counter++;

                if(counter > MAX_MEDIA) return;

                mItem.addImage();

                onMediaSegmentsUpdated();
                onEditTextsUpdated();
            }
        });

        for(int i = 0; i < MAX_MEDIA; i++) {
            editTexts[i].addTextChangedListener(new EditTextListener(i));

            setImageButtons[i].setOnClickListener(new SetMediaListener(i));
            removeImageButtons[i].setOnClickListener(new RemoveMediaListener(i));
        }

        confirmChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRecentlySaved) {
                    saveChanges();
                    Toast.makeText(getActivity(), R.string.changes_already_saved_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                int msg = (mItem.getState() == PUBLISHED) ? (R.string.save_changes_dialog_published) : (R.string.save_changes_dialog);

                showOnClickDialog(R.string.save_changes, R.string.save, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges();
                        Toast.makeText(getActivity(), R.string.save_changes_toast, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItem.getTitle().equals(""))
                    Toast.makeText(getActivity(), R.string.no_title_toast, Toast.LENGTH_SHORT).show();

                else {
                    // saves changes in the db without adding to page
                    mDatabaseManager.pushItemToDB(mItem);
                    mStorageManager.uploadItemImages(mItem, new OnCompleteListener<UploadTask.TaskSnapshot>() {  // upload image to storage
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            mDatabaseManager.refreshItemImage(mItem); // triggers refreshing image
                        }
                    });

                    showOnClickDialog(R.string.publish_article, R.string.publish, R.string.publish_article_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(getActivity(), CreateNotificationActivity.class);
                            i.putExtra(ItemFragment.EXTRA_ID, mItem.getId());
                            startActivity(i);
                        }
                    });
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItem.getTitle().equals(""))
                    Toast.makeText(getActivity(), R.string.no_title_toast, Toast.LENGTH_SHORT).show();

                else {
                    showOnClickDialog(R.string.post_article, R.string.post, R.string.post_article_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showTimeDialogOnStateCreated();
                        }
                    });
                }
            }
        });

        draftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItem.getTitle().equals(""))
                    Toast.makeText(getActivity(), R.string.no_title_toast, Toast.LENGTH_SHORT).show();

                else {
                    showOnClickDialog(R.string.save_draft, R.string.save, R.string.save_draft_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateState(DRAFT);
                            Toast.makeText(getActivity(), R.string.save_draft_toast, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOnClickDialog(R.string.delete_article, R.string.delete, R.string.delete_article_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finish();
                        delete();
                    }
                });
            }
        });

        onStateUpdated();
        onTitleUpdated();
        onImageUpdated();
        onTimeUpdated();
        onEditTextsUpdated();
        onMediaSegmentsUpdated();
        mRecentlySaved = true;        // onStateUpdated() sets Recently Saved to false
    }

    private void launchCrop(Uri uri, int requestCode) {
        String filename = uri.getLastPathSegment();
        Uri destUri = Uri.fromFile(new File(getActivity().getCacheDir(), filename));

        UCrop.of(uri, destUri)
                .withAspectRatio(8, 5)
                .withMaxResultSize(1000, 625)
                .start(getActivity(), this, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode == MAIN_IMAGE_REQUEST) {
                final Uri resultUri = UCrop.getOutput(data);
                mItem.setImageUri(resultUri);
                mItem.setHasImage(true);
                mRecentlySaved = false;
                onImageUpdated(resultUri);
            }

            else if (requestCode < MAX_MEDIA) { // 0 or 1
                final Uri resultUri = UCrop.getOutput(data);
                mItem.getMediaSegments().set(requestCode, resultUri);
                mRecentlySaved = false;
                onMediaSegmentsUpdated();
            }
        }
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

    private void updateState(Item.State state) {
        mItem.setState(state);
        onStateUpdated();
        saveChanges();
        exit();
    }

    private void saveChanges() {
        mRecentlySaved = true;

        mDatabaseManager.pushItemToDB(mItem);  // adds item data to the items table
        mDatabaseManager.addItemToPage(mItem); // adds the item id to the owner page's items list

        mStorageManager.uploadItemImages(mItem, new OnCompleteListener<UploadTask.TaskSnapshot>() {  // upload image to storage
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                mDatabaseManager.refreshItemImage(mItem); // triggers refreshing image
            }
        });
    }

    private void delete() {
        mDatabaseManager.deleteItem(mItem);
        mStorageManager.deleteItem(mItem);
    }

    private class SetMediaListener implements View.OnClickListener {
        int i;
        String setImageString;
        ArrayList<Object> mediaSegments;

        SetMediaListener(int i) {
            this.i = i;
            this.setImageString = getString(getResources().getIdentifier("set_image_" + (i+1), "string", getActivity().getPackageName()));
        }

        @Override
        public void onClick(View v) {
            FragmentManager fm = getActivity().getSupportFragmentManager();

            PickImageDialog.build(new PickSetup()
                    .setTitle(setImageString)
                    .setSystemDialog(true))
                    .setOnPickResult(new IPickResult() {
                        @Override
                        public void onPickResult(PickResult r) {
                            new EventsLogger(getActivity()).log("item_image" + i + "_taken", "error", r.getError() != null ? r.getError().toString() : "No Error");
                            if (r.getError() == null)
                                launchCrop(r.getUri(), i);
                        }
                    }).show(fm);
        }

    }

    private class RemoveMediaListener implements View.OnClickListener {
        int i;

        RemoveMediaListener(int i) {
            this.i = i;
        }

        @Override
        public void onClick(View v) {
            mItem.getMediaSegments().remove(i);
            mItem.removeTextSegment(i+1);
            onMediaSegmentsUpdated();
            onEditTextsUpdated();
        }
    }

    private class EditTextListener implements TextWatcher {
        int i;

        EditTextListener(int i) {
            this.i = i;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(i < mItem.getSegmentsCounter()) {
                mItem.getTextSegments().set(i+1, s.toString());
                mRecentlySaved = false;
                new EventsLogger(getActivity()).log("edit_item_text" + i + "_changed", "new_text", s.toString(), "item_text", mItem.getTextSegments().get(i+1));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }


    private void onStateUpdated() {
        Log.d(TAG, "State: " + mItem.getState().name());

        mRecentlySaved = false; // usually state is updated when it is changed manually by the user. if not, after calling the function set Recently Saved to True

        draftButton.setVisibility(View.GONE);
        createButton.setVisibility(View.GONE);
        publishButton.setVisibility(View.GONE);
        publishedTextView.setVisibility(View.GONE);

        saveChangesLayout.setVisibility(View.VISIBLE);

        switch (mItem.getState()) {
            case NEW:
                saveChangesLayout.setVisibility(View.GONE);
                draftButton.setVisibility(View.VISIBLE);

            case DRAFT:
                createButton.setVisibility(View.VISIBLE);

            case CREATED:
                publishButton.setVisibility(View.VISIBLE);
                break;

            case PUBLISHED:
                publishedTextView.setVisibility(View.VISIBLE);
        }
    }

    private void onTimeUpdated() {
        timeTextView.setText(mItem.getTime());
        mRecentlySaved = false;
    }

    private void onEditTextsUpdated() {
        new EventsLogger(getActivity()).log("on_edit_texts_updated", "item_text", mItem.getText());
        mainEditText.setText(mItem.getText());
        for(int i = 0; i < mItem.getSegmentsCounter(); i++) {
            new EventsLogger(getActivity()).log("on_edit_texts" + i +"_updated", "item_text" + i, mItem.getTextSegments().get(i+1));
            editTexts[i].setText(mItem.getTextSegments().get(i+1));
        }
    }

    private void onMediaSegmentsUpdated() {
        for(int i = 0; i < MAX_MEDIA; i++) {
            editTexts[i].setVisibility(View.GONE);
            imageLinearLayouts[i].setVisibility(View.GONE);
            addPhotoButton.setVisibility(View.GONE);

            if(i < mItem.getSegmentsCounter()) {
                imageLinearLayouts[i].setVisibility(View.VISIBLE);
                editTexts[i].setVisibility(View.VISIBLE);

                final int index = i;
                Uri localMediaUri = mItem.getMediaSegments().get(i);

                if(localMediaUri != null) {
                    imageImageViews[index].setImageURI(localMediaUri);
                }

                else {
                    final String filename = "image" + i + ".png";

                    FirebaseStorage.getInstance().getReference("items").child(mItem.getId().toString()).child(filename).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (getActivity() == null) return;

                            if (task.getException() == null && index < mItem.getSegmentsCounter()) {
                                Glide.with(getActivity()).load(task.getResult()).into(imageImageViews[index]);
                            } else { // No Image
                                imageImageViews[index].setImageResource(R.drawable.image_template);
                            }
                        }
                    });
                }
            }

            if(mItem.getSegmentsCounter() < MAX_MEDIA) {
                addPhotoButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onTitleUpdated() {
        new EventsLogger(getActivity()).log("on_title_updated", "item_title", mItem.getTitle(), "item_author", mItem.getAuthor());
        titleEditText.setText(mItem.getTitle());
        authorEditText.setText(mItem.getAuthor());
    }

    private void onImageUpdated(Uri localImageUri) {
        if(localImageUri != null) {
            imageView.setImageURI(localImageUri);
            removeImageButton.setVisibility(View.VISIBLE);
        }

        else
            onImageUpdated();
    }

    private void onImageUpdated() {
        FirebaseStorage.getInstance().getReference("items").child(mItem.getId().toString()).child("image.png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(getActivity() == null) return;

                if (task.getException() == null) {
                    Glide.with(getActivity()).load(task.getResult()).into(imageView);
                    removeImageButton.setVisibility(View.VISIBLE);
                } else { // No Image
                    imageView.setImageResource(R.drawable.image_template);
                    removeImageButton.setVisibility(View.GONE);
                }
            }
        });

        mRecentlySaved = false;
    }


    private void showOnClickDialog(int title, int positiveButtonString, int msg, DialogInterface.OnClickListener onClickListener) {

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(positiveButtonString, onClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        alertDialog.show();
    }

    // Not to confuse with showTimeDialogOn"SetTime"ButtonClicked
    // Shows Time Dialog when state changes to "Created"
    private void showTimeDialogOnStateCreated() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.set_time)
                .setMessage(R.string.publication_time_dialog_state_changed)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mItem.setCurrentTime();
                        onTimeUpdated();

                        updateState(CREATED);
                        Toast.makeText(getActivity(), R.string.post_article_toast, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateState(CREATED);
                        Toast.makeText(getActivity(), R.string.post_article_toast, Toast.LENGTH_SHORT).show();
                    }
                })
                .create();

        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_edit_item, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem saveItem = menu.getItem(0);
        MenuItem draftItem = menu.getItem(1);
        MenuItem createItem = menu.getItem(2);
        MenuItem publishItem = menu.getItem(3);

        draftItem.setVisible(false);
        createItem.setVisible(false);
        publishItem.setVisible(false);
        saveItem.setVisible(true);

        if(mItem == null) return;

        switch (mItem.getState()) {
            case NEW:
                saveItem.setVisible(false);
                draftItem.setVisible(true);

            case DRAFT:
                createItem.setVisible(true);

            case CREATED:
                publishItem.setVisible(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        else if(item.getItemId() == R.id.menu_item_save_draft)
            draftButton.performClick();

        else if(item.getItemId() == R.id.menu_item_post_article)
            createButton.performClick();

        else if(item.getItemId() == R.id.menu_item_publish_article)
            publishButton.performClick();

        else if(item.getItemId() == R.id.menu_item_save_changes) {
            if(mRecentlySaved) {
                saveChanges();
                Toast.makeText(getActivity(), R.string.changes_already_saved_toast, Toast.LENGTH_SHORT).show();
                return true;
            }

            int msg = (mItem.getState() == PUBLISHED) ? (R.string.save_changes_dialog_published) : (R.string.save_changes_dialog);

            showOnClickDialog(R.string.save_changes, R.string.save, msg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveChanges();
                    Toast.makeText(getActivity(), R.string.save_changes_toast, Toast.LENGTH_SHORT).show();
                    exit();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    public void exit() {
        if (NavUtils.getParentActivityName(getActivity()) != null) {
            Intent intent = NavUtils.getParentActivityIntent(getActivity());
            intent.putExtra(ItemFragment.EXTRA_ID, mItem.getId());

            NavUtils.navigateUpTo(getActivity(), intent);
        }
    }

    interface BackListener {
         void exit();
    }

    public void onBackPressed() {

        if(mRecentlySaved) {
            exit();

            if(mItem.getState() == NEW)
                delete(); // item hasn't been added to his owner page but was added to the items db, and shall be deleted

            return;
        }

        if(mItem.getState() == NEW) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.exit)
                    .setMessage(R.string.exit_edit_dialog_new)
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exit();
                            delete(); // item should not be attached to any page yet, but just in case
                        }
                    })
                    .setNegativeButton(R.string.save_draft, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateState(DRAFT);
                            exit();
                        }
                    })
                    .setNeutralButton(android.R.string.cancel, null)
                    .create();

            alertDialog.show();
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.exit)
                .setMessage(R.string.exit_edit_dialog)
                .setPositiveButton(R.string.exit_no_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exit();
                    }
                })
                .setNegativeButton(R.string.exit_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges();
                        exit();
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .create();

        alertDialog.show();
    }


    public static EditItemFragment newInstance(UUID id) {

        Bundle args = new Bundle();
        args.putSerializable(ItemFragment.EXTRA_ID, id);

        EditItemFragment fragment = new EditItemFragment();
        fragment.setArguments(args);

        return fragment;
    }
}