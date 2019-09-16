package com.liadk.android.pushit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginSettingsFragment extends PreferenceFragmentCompat {

    private static final String ACCOUNT_LOGIN = "accountLogin";
    private static final String CREATE_ACCOUNT = "createAccount";

    private FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;

    private Preference mLoginPreference;
    private Preference mSignUpPreference;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.settings);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_login);
        mLoginPreference = findPreference(ACCOUNT_LOGIN);
        mSignUpPreference = findPreference(CREATE_ACCOUNT);

        mLoginPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return true;
            }
        });

        mSignUpPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), CreateAccountActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
