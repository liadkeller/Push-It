package com.liadk.android.pushit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class ContentCreatorSettingsFragment extends PreferenceFragmentCompat {

    private static final String MY_ACCOUNT = "myAccount";
    private static final String ACCOUNT_EMAIL = "accountEmail";
    private static final String ACCOUNT_STATUS = "accountStatus";
    private static final String PAGE_SETTINGS = "pageSettings";
    private static final String PAGE_DELETE = "pageDelete";
    private static final String ACCOUNT_SIGN_OUT = "signOut";
    private static final String ACCOUNT_DELETE = "accountDelete";

    private static final String DIALOG_PAGE = "pageDialog";
    private static final int REQUEST_PAGE_DETAILS = 0;


    private FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;

    private PreferenceCategory mMyAccountCategory;
    private Preference mEmailPreference;
    private SwitchPreference mStatusPreference;
    private Preference mPageSettingsPreference;
    private Preference mDeletePagePreference;
    private Preference mSignOutPreference;
    private Preference mDeleteAccountPreference;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.settings);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        mDatabaseManager = DatabaseManager.get(getActivity());
        mAuth = FirebaseAuth.getInstance();

        addPreferencesFromResource(R.xml.preferences_app_content_creator);

        mMyAccountCategory = (PreferenceCategory) findPreference(MY_ACCOUNT);
        mEmailPreference = findPreference(ACCOUNT_EMAIL);
        mStatusPreference = (SwitchPreference) findPreference(ACCOUNT_STATUS);
        mPageSettingsPreference = findPreference(PAGE_SETTINGS);
        mDeletePagePreference = findPreference(PAGE_DELETE);
        mSignOutPreference = findPreference(ACCOUNT_SIGN_OUT);
        mDeleteAccountPreference = findPreference(ACCOUNT_DELETE);


        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null) {
            replaceFragment(null);
            return;
        }

        final String userId = user.getUid();

        mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);

                if(replaceFragment(user)) return;
                updatePreferences(user, userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // replaced fragment according to user status if necessary
    private boolean replaceFragment(PushItUser user) {

        Fragment fragment = null;

        if(user == null)
            fragment = new LoginSettingsFragment();

        else if(!user.getStatus())
            fragment = new SettingsFragment();

        else
            return false; // user status = false (content follower) thus current fragment ok

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        return true;
    }

    private void refreshUserStatus(PushItUser user) {
        replaceFragment(user);
        ((HomeActivity) getActivity()).getUserStatus();
    }

    protected void updatePreferences(final PushItUser user, final String userId) {
        mEmailPreference.setTitle(user.getEmail());

        int statusSummary = (user.getStatus()) ? R.string.status_creator : R.string.status_follower;
        mStatusPreference.setChecked(user.getStatus());
        mStatusPreference.setSummary(statusSummary);

        mStatusPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean status = (boolean) newValue;

                if(status)
                    showCreatePageDialog();

                return false;
            }
        });

        mPageSettingsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), PageSettingsActivity.class);
                i.putExtra(PageFragment.EXTRA_ID, UUID.fromString(user.getPageId()));
                startActivity(i);
                return true;
            }
        });

        mDeletePagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.delete_page)
                        .setMessage(R.string.delete_page_dialog)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // mDatabaseManager.deletePage(user.getPageId()); // TODO delete user data from db
                                user.setContentFollower();

                                mDatabaseManager.pushUserToDB(user, userId, new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        refreshUserStatus(user);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();

                alertDialog.show();
                return true;
            }
        });

        mSignOutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.sign_out)
                        .setMessage(R.string.sign_out_dialog)
                        .setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ProgressDialog progressDialog = showProgressDialog(getString(R.string.sign_out_progress_dialog));
                                mAuth.signOut();
                                dismissProgressDialog(progressDialog, 700);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();

                alertDialog.show();
                return true;
            }

            private ProgressDialog showProgressDialog(String string) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(string);
                progressDialog.show();
                return progressDialog;
            }

            private void dismissProgressDialog(final ProgressDialog progressDialog, final int delayMillis) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                refreshUserStatus(null);
                            }
                        }, delayMillis);
            }
        });

        mDeleteAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int dialogMsg = mStatusPreference.isChecked() ? R.string.delete_account_dialog_creator : R.string.delete_account_dialog_follower;

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.delete_account)
                        .setMessage(dialogMsg)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDatabaseManager.deleteUser(user, userId); // delete user data from db

                                // delete user from authentication data source
                                if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid() == userId)
                                    mAuth.getCurrentUser().delete();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();

                alertDialog.show();
                return true;
            }
        });
    }

    private void showCreatePageDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_image, null);
        ((ImageView) v.findViewById(R.id.dialogImageView)).setImageResource(R.drawable.pushit_banner);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.continue_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                Bundle args = new Bundle();
                args.putBoolean(PageDetailsDialogFragment.ARG_NAME_ERROR, false);

                PageDetailsDialogFragment pageDetailsDialogFragment = new PageDetailsDialogFragment();
                pageDetailsDialogFragment.setArguments(args);
                pageDetailsDialogFragment.setTargetFragment(
                        ContentCreatorSettingsFragment.this, REQUEST_PAGE_DETAILS);
                pageDetailsDialogFragment.show(fm, DIALOG_PAGE);

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_PAGE_DETAILS) {
            if(resultCode == Activity.RESULT_OK) {
                String pageName = data.getStringExtra(SettingsFragment.PageDetailsDialogFragment.EXTRA_NAME);
                Page newPage = new Page(pageName);

                mDatabaseManager.pushPageToDB(newPage);

                final String pageId = newPage.getId().toString();

                mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mAuth.getCurrentUser() == null) return;

                        String userId = mAuth.getCurrentUser().getUid();
                        final PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);
                        user.addPage(pageId); // from here, user status is 'Creator'

                        mDatabaseManager.pushUserToDB(user, userId, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                ((HomeActivity) getActivity()).getUserStatus();

                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.page_created)
                                        .setMessage(R.string.create_page_done_dialog)
                                        .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                replaceFragment(user);

                                                // launch Page Settings Activity of the new page
                                                Intent i = new Intent(getActivity(), PageSettingsActivity.class);
                                                i.putExtra(PageFragment.EXTRA_ID, UUID.fromString(pageId));
                                                startActivity(i);
                                            }
                                        })
                                        .create();

                                alertDialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            else if(resultCode == Activity.RESULT_CANCELED) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                Bundle args = new Bundle();
                args.putBoolean(PageDetailsDialogFragment.ARG_NAME_ERROR, true);

                PageDetailsDialogFragment pageDetailsDialogFragment = new PageDetailsDialogFragment();
                pageDetailsDialogFragment.setArguments(args);
                pageDetailsDialogFragment.setTargetFragment(ContentCreatorSettingsFragment.this, REQUEST_PAGE_DETAILS);
                pageDetailsDialogFragment.show(fm, DIALOG_PAGE);
            }
        }
    }

    public static class PageDetailsDialogFragment extends DialogFragment {

        private static final String EXTRA_NAME = "name";
        private static final String EXTRA_DESC = "description";

        private static final String ARG_NAME_ERROR = "nameError";

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_page_details, null);

            final EditText pageNameEditText = v.findViewById(R.id.pageNameEditText);

            if(getArguments().getBoolean(ARG_NAME_ERROR)) {
                pageNameEditText.setError("You must enter a valid page name"); // TODO String Resource
            }

            return new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setTitle(R.string.page_name)
                    .setPositiveButton(R.string.create_new_page, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendDetails(pageNameEditText.getText().toString());
                            dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        private void sendDetails(String name) {
            if(name.isEmpty()) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                return;
            }

            Intent data = new Intent();
            data.putExtra(EXTRA_NAME, name);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }
    }
}
