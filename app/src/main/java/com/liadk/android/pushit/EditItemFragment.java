package com.liadk.android.pushit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.liadk.android.pushit.Item.State;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;
import java.util.UUID;

import static com.liadk.android.pushit.Item.State.CREATED;
import static com.liadk.android.pushit.Item.State.DRAFT;
import static com.liadk.android.pushit.Item.State.NEW;
import static com.liadk.android.pushit.Item.State.PUBLISHED;

/**
 * Created by user on 10/08/2019.
 */
public class EditItemFragment extends Fragment implements EditItemActivity.OnBackPressedListener {
    private static final String TAG = "EditItemFragment";
    private static final int PICK_IMAGE = 0;
    private static final int CREATE_NOTIFICATION_REQUEST = 1;

    private Item mItem;
    private Item mOriginalItem;

    private boolean mRecentlySaved = true;


    private EditText titleEditText;
    private EditText authorEditText;
    private EditText mainEditText;

    private Button setImageButton;
    private ImageView imageView;

    private Button setTimeButton;
    private TextView timeTextView;

    private Button[] setImageButtons;
    private Button[] setVideoButtons;
    private Button[] removeImageButtons;
    private Button[] removeVideoButtons;
    private ImageView[] imageImageViews;
    private SurfaceView[] videoSurfaceViews;
    private LinearLayout[] imageLinearLayouts;
    private LinearLayout[] videoLinearLayouts;
    private EditText[] editTexts;
    protected final static int MAX_MEDIA = 2; // maximum media segments

    private Button addPhotoButton;
    private Button addVideoButton;

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

        UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);
        mOriginalItem = ItemCollection.get(getActivity()).getItem(id);
        mItem = mOriginalItem.getEditItem();

        ((EditItemActivity) getActivity()).setOnBackPressedListener((EditItemFragment) this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_item, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Referencing Widgets
        titleEditText = (EditText) v.findViewById(R.id.titleEditText);
        authorEditText = (EditText) v.findViewById(R.id.authorEditText);
        mainEditText = (EditText) v.findViewById(R.id.mainItemEditText);

        setImageButton = (Button) v.findViewById(R.id.setImageButton);
        imageView = (ImageView) v.findViewById(R.id.itemImageView);

        setTimeButton = (Button) v.findViewById(R.id.setTimeButton);
        timeTextView = (TextView) v.findViewById(R.id.cardTime);

        setImageButtons = new Button[] { (Button) v.findViewById(R.id.setImage1Button), (Button) v.findViewById(R.id.setImage2Button) };
        setVideoButtons = new Button[] { (Button) v.findViewById(R.id.setVideo1Button), (Button) v.findViewById(R.id.setVideo2Button) };
        removeImageButtons = new Button[] { (Button) v.findViewById(R.id.removeImage1Button), (Button) v.findViewById(R.id.removeImage2Button) };
        removeVideoButtons = new Button[] { (Button) v.findViewById(R.id.removeVideo1Button), (Button) v.findViewById(R.id.removeVideo2Button) };
        imageImageViews = new ImageView[] { (ImageView) v.findViewById(R.id.image1ImageView), (ImageView) v.findViewById(R.id.image2ImageView)};
        videoSurfaceViews = new SurfaceView[] { (SurfaceView) v.findViewById(R.id.video1SurfaceView), (SurfaceView) v.findViewById(R.id.video2SurfaceView)};

        imageLinearLayouts = new LinearLayout[] { (LinearLayout) v.findViewById(R.id.image1LinearLayout), (LinearLayout) v.findViewById(R.id.image2LinearLayout)};
        videoLinearLayouts = new LinearLayout[] { (LinearLayout) v.findViewById(R.id.video1LinearLayout), (LinearLayout) v.findViewById(R.id.video2LinearLayout)};
        editTexts          = new EditText[]     { (EditText) v.findViewById(R.id.seg1EditText), (EditText) v.findViewById(R.id.seg2EditText) };

        addPhotoButton = (Button) v.findViewById(R.id.addPhotoButton);
        addVideoButton = (Button) v.findViewById(R.id.addVideoButton);

        saveChangesLayout = (LinearLayout) v.findViewById(R.id.saveChangesLayout);
        confirmChangesButton = (Button) v.findViewById(R.id.confirmChangesButton);

        publishedTextView = (TextView) v.findViewById(R.id.publishedTextView);
        publishButton = (Button) v.findViewById(R.id.publishButton);
        createButton = (Button) v.findViewById(R.id.createButton);
        draftButton = (Button) v.findViewById(R.id.draftButton);
        deleteButton = (Button) v.findViewById(R.id.deleteButton);


        // Configure
        titleEditText.setText(mItem.getTitle());
        authorEditText.setText(mItem.getAuthor());
        mainEditText.setText(mItem.getText());
        timeTextView.setText(mItem.getTime());
        imageView.setImageBitmap(mItem.getImage());

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mItem.setTitle(s.toString());
                mRecentlySaved = false;
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
                                if(r.getError() == null) {
                                    mItem.setImage(r.getBitmap());
                                    onImageUpdated();
                                }
                            }
                        })
                        .show(fm);

            /*
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("scale", true);
                intent.putExtra("aspectX", 16);
                intent.putExtra("aspectY", 9);
                startActivityForResult(intent, PICK_IMAGE);
                */
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

                mItem.addImage(null);
                // TODO take care of mRecentlySaved (adding field should not trigger this boolean, but editing the text or setting an image should)

                onMediaSegmentsUpdated();
                onEditTextsUpdated();
            }
        });

        addVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int counter = mItem.getSegmentsCounter();
                counter++;

                if(counter > MAX_MEDIA) return;

                mItem.addVideo(new MediaStore.Video());
                // TODO take care of mRecentlySaved (adding field should not trigger this boolean, but editing the text or setting an image should)

                onMediaSegmentsUpdated();
                onEditTextsUpdated();
            }
        });

        for(int i = 0; i < MAX_MEDIA; i++) {
            setImageButtons[i].setOnClickListener(new SetMediaListener(i, true));
            // TODO setVideoButtons[i].setOnClickListener(new SetMediaListener(i, false));
            removeImageButtons[i].setOnClickListener(new RemoveMediaListener(i));
            removeVideoButtons[i].setOnClickListener(new RemoveMediaListener(i));
            editTexts[i].addTextChangedListener(new EditTextListener(i));
        }

        confirmChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mRecentlySaved) {
                    saveChanges();
                    Toast.makeText(getActivity(), R.string.changes_saved_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                int msg = (mItem.getState() == PUBLISHED) ? (R.string.save_changes_dialog_published) : (R.string.save_changes_dialog);

                showClickDialog(R.string.save_changes, R.string.save, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges();
                        Toast.makeText(getActivity(), R.string.save_changes_toast, Toast.LENGTH_SHORT).show();
                        exit();
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
                    showClickDialog(R.string.publish_article, R.string.publish, R.string.publish_article_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(getActivity(), CreateNotificationActivity.class);
                            i.putExtra(ItemFragment.EXTRA_ID, mOriginalItem.getId());
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
                    showClickDialog(R.string.post_article, R.string.post, R.string.post_article_dialog, new DialogInterface.OnClickListener() {
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
                    showClickDialog(R.string.save_draft, R.string.save, R.string.save_draft_dialog, new DialogInterface.OnClickListener() {
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
                showClickDialog(R.string.delete_article, R.string.delete, R.string.delete_article_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete();
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finish();
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

        return v;
    }

    private void updateState(Item.State newState) {
        Item.State oldState = mItem.getState();
        mItem.setState(newState);
        onStateUpdated();
        saveChanges();
        mOriginalItem.addToPage(oldState);
        exit();
    }

    private void saveChanges() {
        mRecentlySaved = true;
        mOriginalItem.edit();
        // getActivity().setResult(Activity.RESULT_OK, null); TODO track on: (setResult(Activity.RESULT_OK))
    }

    // deletes this item from ItemCollection and from it's owner
    private void delete() {
        Page owner = mOriginalItem.getOwner();
        if(owner != null)
            owner.removeItem(mOriginalItem);
        ItemCollection.get(getActivity()).delete(mItem);
    }

    private class SetMediaListener implements View.OnClickListener {
        int i;
        boolean isImage;
        String setImage;
        String setVideo;
        ArrayList<Object> mediaSegments = mItem.getMediaSegments();

        public SetMediaListener(int i, boolean isImage) {
            this.i = i;
            this.isImage = isImage;
            if(this.isImage)
                setImage = getString(getResources().getIdentifier("set_image_" + (i+1), "string", getActivity().getPackageName()));
            else
                setVideo = getString(getResources().getIdentifier("set_video_" + (i+1), "string", getActivity().getPackageName()));
        }

        @Override
        public void onClick(View v) {
            FragmentManager fm = getActivity().getSupportFragmentManager();

            if(isImage) {
                PickImageDialog.build(new PickSetup()
                        .setTitle(setImage)
                        .setSystemDialog(true))
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                if (r.getError() == null) {
                                    mediaSegments.set(i, r.getBitmap());
                                    onMediaSegmentsUpdated();
                                }
                            }
                        }).show(fm);
            }

            else {
                PickImageDialog.build(new PickSetup()
                        .setTitle(setVideo)
                        .setSystemDialog(true))
                        //.setVideo(true)) TODO Enable Video
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                if (r.getError() == null) {
                                    mediaSegments.set(i, r.getBitmap());
                                    onMediaSegmentsUpdated();
                                }
                            }
                        }).show(fm);
            }
        }

    }

    private class RemoveMediaListener implements View.OnClickListener {
        int i;

        public RemoveMediaListener(int i) {
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

        public EditTextListener(int i) {
            this.i = i;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(i < mItem.getSegmentsCounter()) {
                mItem.getTextSegments().set(i+1, s.toString());
                mRecentlySaved = false;
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
        mainEditText.setText(mItem.getText());
        for(int i = 0; i < mItem.getSegmentsCounter(); i++) {
            editTexts[i].setText(mItem.getTextSegments().get(i+1));
        }
    }

    private void onMediaSegmentsUpdated() {
        for(int i = 0; i < MAX_MEDIA; i++) {

            imageLinearLayouts[i].setVisibility(View.GONE);
            videoLinearLayouts[i].setVisibility(View.GONE);
            editTexts[i].setVisibility(View.GONE);
            addPhotoButton.setVisibility(View.GONE);
            addVideoButton.setVisibility(View.GONE);

            if(i < mItem.getSegmentsCounter()) {
                Object mediaSegment = mItem.getMediaSegments().get(i);

                if (mediaSegment instanceof MediaStore.Video) {
                    videoLinearLayouts[i].setVisibility(View.VISIBLE);
                    if(mediaSegment != null) {
                        // TODO add Video
                    }
                }
                else {
                    imageLinearLayouts[i].setVisibility(View.VISIBLE);
                    if(mediaSegment != null && mediaSegment instanceof Bitmap) {
                        imageImageViews[i].setImageBitmap((Bitmap) mediaSegment);
                    }
                }
                editTexts[i].setVisibility(View.VISIBLE);
            }

            if(mItem.getSegmentsCounter() < MAX_MEDIA) {
                addPhotoButton.setVisibility(View.VISIBLE);
                addVideoButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onTitleUpdated() {
        titleEditText.setText(mItem.getTitle());
        authorEditText.setText(mItem.getAuthor());
    }

    private void onImageUpdated() {
        imageView.setImageBitmap(mItem.getImage());
        mRecentlySaved = false;
    }


    private void showClickDialog(int title, int positiveButtonString, int msg, DialogInterface.OnClickListener onClickListener) {

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

        saveItem.setVisible(false);

        draftItem.setVisible(false);
        createItem.setVisible(false);
        publishItem.setVisible(false);
        saveItem.setVisible(true);

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

        else if(item.getItemId() == R.id.menu_item_save_changes)
            confirmChangesButton.performClick();

        else if(item.getItemId() == R.id.menu_item_save_draft)
            draftButton.performClick();

        else if(item.getItemId() == R.id.menu_item_post_article)
            createButton.performClick();

        else if(item.getItemId() == R.id.menu_item_publish_article)
            publishButton.performClick();

        return super.onOptionsItemSelected(item);
    }

    public void exit() {
        if (NavUtils.getParentActivityName(getActivity()) != null) {
            Intent intent = NavUtils.getParentActivityIntent(getActivity());
            intent.putExtra(ItemFragment.EXTRA_ID, mOriginalItem.getId());

            NavUtils.navigateUpTo(getActivity(), intent);
        }
    }

    interface BackListener {
         void exit();
    }

    public void onBackPressed() {

        if(mRecentlySaved) {
            exit();
            return;
        }

        if(mItem.getState() == NEW) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.exit)
                    .setMessage(R.string.exit_edit_dialog_new)
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            delete(); // item should not be attached to any page yet, but just in case
                            exit();
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
                        mOriginalItem.setNewEditItem();
                        mItem = mOriginalItem.getEditItem();
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