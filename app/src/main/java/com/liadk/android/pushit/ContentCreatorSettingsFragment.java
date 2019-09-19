package com.liadk.android.pushit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class ContentCreatorSettingsFragment extends SettingsFragment {

    protected static final String PAGE_SETTINGS = "pageSettings";
    protected static final String PAGE_DELETE = "pageDelete";


    protected FirebaseAuth mAuth;
    protected DatabaseManager mDatabaseManager;

    protected PreferenceCategory mMyAccountCategory;
    protected Preference mEmailPreference;
    protected SwitchPreference mStatusPreference;
    protected Preference mPageSettingsPreference;
    protected Preference mDeletePagePreference;
    protected Preference mSignOutPreference;
    protected Preference mDeleteAccountPreference;

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
                configurePreferences(user, userId);
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

    protected void refreshUserStatus(PushItUser user) {
        replaceFragment(user);
        ((HomeActivity) getActivity()).getUserStatus();
    }

    // configure both account preferences as well as page preferences
    protected void configurePreferences(final PushItUser user, final String userId) {
        configureAccountPreferences(user, userId);

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
                                final ProgressDialog progressDialog = showProgressDialog(getString(R.string.delete_page_progress_dialog));
                                // mDatabaseManager.deletePage(user.getPageId()); // TODO delete user data from db
                                user.setContentFollower();

                                mDatabaseManager.pushUserToDB(user, userId, new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        dismissProgressDialog(progressDialog, user, DELAY);
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
    }

    protected void configureAccountPreferences(final PushItUser user, final String userId) {
        mEmailPreference.setTitle(user.getEmail());

        mStatusPreference.setChecked(true);
        mStatusPreference.setSummary(R.string.status_creator);

        mStatusPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return false;
            }
        });

        mSignOutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showOnClickDialog(R.string.sign_out, R.string.sign_out, R.string.sign_out_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressDialog progressDialog = showProgressDialog(getString(R.string.sign_out_progress_dialog));
                        mAuth.signOut();
                        dismissProgressDialog(progressDialog, null, DELAY);
                    }
                });

                return true;
            }
        });

        mDeleteAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int dialogMsg = mStatusPreference.isChecked() ? R.string.delete_account_dialog_creator : R.string.delete_account_dialog_follower;

                showOnClickDialog(R.string.delete_account, R.string.delete, dialogMsg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressDialog progressDialog = showProgressDialog(getString(R.string.delete_account_progress_dialog));
                        mDatabaseManager.deleteUser(user, userId); // delete user data from db

                        // delete user from authentication data source
                        if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid() == userId)
                            mAuth.getCurrentUser().delete();

                        dismissProgressDialog(progressDialog, null, DELAY);
                    }
                });

                return true;
            }
        });
    }
}
