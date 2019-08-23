package com.liadk.android.pushit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

public class PageSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Page mPage;
    private ListPreference mDesignPreference;
    private EditTextPreference mNamePreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.page_settings);

        UUID id = (UUID) getArguments().getSerializable(PageFragment.EXTRA_ID);
        mPage = PageCollection.get(getActivity()).getPage(id);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_page);
        mNamePreference = (EditTextPreference) getPreferenceScreen().findPreference("pageName");
        mDesignPreference = (ListPreference) getPreferenceScreen().findPreference("listDesign");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
                //inflater.inflate(R.layout.fragment_page_settings, container, false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mNamePreference.setText(mPage.getName());
        mNamePreference.setSummary(mPage.getName());
        if(mPage.getName().equals(""))
            mNamePreference.setSummary(R.string.choose_name_summary);

        //mPage.settings.design = Page.Design.getDesign(mDesignPreference.getEntry().toString()); // TODO Check null

        int index = mDesignPreference.findIndexOfValue(mPage.settings.design.toString());
        mDesignPreference.setValueIndex(index);
        mDesignPreference.setSummary(mDesignPreference.getEntry());

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("listDesign")) {
            mPage.settings.design = Page.Design.getDesign(mDesignPreference.getEntry().toString());
            mDesignPreference.setSummary(mDesignPreference.getEntry());
        }

        else if (key.equals("pageName")) {
            mPage.setName(mNamePreference.getText());
            mNamePreference.setSummary(mNamePreference.getText());
        }
    }

    public static Fragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ItemFragment.EXTRA_ID, id);

        PageSettingsFragment fragment = new PageSettingsFragment();
        fragment.setArguments(args);

        return (Fragment) fragment;
    }
}
