package com.liadk.android.pushit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AccountSettingsFragment extends PreferenceFragmentCompat {

    static final String KEY_EMAIL_PREFERENCE = "emailPreference";

    protected static final String ACCOUNT_EMAIL = "accountEmail";
    protected static final String UPDATE_EMAIL = "updateEmail";
    protected static final String UPDATE_PASSWORD = "updatePassword";

    protected static final int DELAY = 700;


    protected FirebaseAuth mAuth;
    protected DatabaseManager mDatabaseManager;

    protected Preference mEmailPreference;
    protected Preference mUpdateEmailPreference;
    protected Preference mUpdatePasswordPreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.account_settings);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        mDatabaseManager = DatabaseManager.get(getActivity());
        mAuth = FirebaseAuth.getInstance();

        addPreferencesFromResource(R.xml.preferences_account);

        mEmailPreference = findPreference(ACCOUNT_EMAIL);
        mUpdateEmailPreference = findPreference(UPDATE_EMAIL);
        mUpdatePasswordPreference = findPreference(UPDATE_PASSWORD);

        String email = (getActivity() != null) ? PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(KEY_EMAIL_PREFERENCE, "") : null;
        mEmailPreference.setTitle(email);


        mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser() == null) return;

                final String userId = mAuth.getCurrentUser().getUid();
                PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);

                configureAccountPreferences(user, userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    protected void configureAccountPreferences(final PushItUser user, final String userId) {
        mEmailPreference.setTitle(user.getEmail());
        if(getActivity() != null)
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit()
                .putString(KEY_EMAIL_PREFERENCE, user.getEmail())
                .commit();

        mUpdateEmailPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), UpdateEmailActivity.class);
                startActivity(intent);
                return true;
            }
        });

        mUpdatePasswordPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), UpdatePasswordActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
