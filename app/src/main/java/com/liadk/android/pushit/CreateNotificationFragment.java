package com.liadk.android.pushit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.UUID;

public class CreateNotificationFragment extends Fragment implements EditItemActivity.OnBackPressedListener {

    private Item mItem;
    private PushNotification mNotification;


    private EditText mTitleEditText;
    private CheckBox mTitleCheckBox;
    private ImageView mImageView;

    private Button mImageButton;
    private Button mPublishButton;


    private class PushNotification {
        private String mEditTextTitle = new String();
        private String mItemTitle;
        private boolean mSame = false;
        private Bitmap mImage;

        public PushNotification(Item item) {
            mItemTitle = item.getTitle();
            mImage = item.getImage();
        }

        public void setEditTextTitle(String mTitle) {
            this.mEditTextTitle = mTitle;
        }

        public void setSame(boolean mSame) {
            this.mSame = mSame;
        }

        public void setImage(Bitmap mImage) {
            this.mImage = mImage;
        }

        public String getEditTextTitle() {
            return mEditTextTitle;
        }

        public boolean isSame() {
            return mSame;
        }

        public Bitmap getImage() {
            return mImage;
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

        UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);
        mItem = ItemCollection.get(getActivity()).getItem(id);
        mNotification = new PushNotification(mItem);
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
                    mTitleEditText.setText(mItem.getTitle());
                    mNotification.setSame(true);
                }

                else {
                    mTitleEditText.setEnabled(true);
                    mTitleEditText.setText(mNotification.getEditTextTitle());
                    mNotification.setSame(false);
                }
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
                                if(r.getError() == null) {
                                    mNotification.setImage(r.getBitmap());
                                    onImageUpdated();
                                }
                            }
                        }).show(fm);
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

        onImageUpdated();

        return v;
    }

    private void onImageUpdated() {
        Bitmap image = mNotification.getImage();
        if(image != null) {
            mImageView.setImageBitmap(image);
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

    private void updateState() {
        // Update(PUBLISHED) is here
        mItem.addToPage(mItem.getState());
        mItem.setState(Item.State.PUBLISHED);
        Toast.makeText(getActivity(), R.string.publish_article_toast, Toast.LENGTH_SHORT).show();

        // exits two layers
        Intent intent = new Intent(getActivity(), ItemActivity.class);
        intent.putExtra(ItemFragment.EXTRA_ID, mItem.getId());
        NavUtils.navigateUpTo(getActivity(), intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
