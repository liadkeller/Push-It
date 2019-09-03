package com.liadk.android.pushit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String ACCOUNT_LOGIN = "accountLogin";
    private static final String ACCOUNT_STATUS = "accountStatus";

    private Preference mLoginPreference;
    private SwitchPreference mStatusPreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.settings);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_app);
        mStatusPreference = (SwitchPreference) findPreference(ACCOUNT_STATUS);
        mLoginPreference = findPreference(ACCOUNT_LOGIN);
        mLoginPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
                return true;
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);  // Set up a listener whenever a key changes

    }


    private void updatePreferences() {
        boolean accountStatus = ((HomeActivity) getActivity()).getStatus();
        mStatusPreference.setChecked(accountStatus);
        String summary = (mStatusPreference.isChecked()) ? "Content Creator" : "Content Follower";
        mStatusPreference.setSummary(summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);  // Unregister the listener whenever a key changes
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(ACCOUNT_STATUS)) {
            ((HomeActivity) getActivity()).setStatus(mStatusPreference.isChecked());
            String summary = (mStatusPreference.isChecked()) ? "Content Creator" : "Content Follower";
            mStatusPreference.setSummary(summary);
        }
    }
}
