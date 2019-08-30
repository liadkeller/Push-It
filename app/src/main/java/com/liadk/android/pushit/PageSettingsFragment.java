package com.liadk.android.pushit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.UUID;

public class PageSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PAGE_NAME = "pageName";
    private static final String PAGE_DESC = "pageDesc";
    private static final String PAGE_LAYOUT = "pageLayout";
    private static final String PAGE_LOGO = "pageLogo";

    private Page mPage;
    private ListPreference mLayoutPreference;
    private EditTextPreference mNamePreference;
    private EditTextPreference mDescPreference;
    private Preference mLogoPreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.page_settings);

        UUID id = (UUID) getArguments().getSerializable(PageFragment.EXTRA_ID);
        mPage = PageCollection.get(getActivity()).getPage(id);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_page);
        mNamePreference = (EditTextPreference) getPreferenceScreen().findPreference(PAGE_NAME);
        mDescPreference = (EditTextPreference) getPreferenceScreen().findPreference(PAGE_DESC);
        mLayoutPreference = (ListPreference) getPreferenceScreen().findPreference(PAGE_LAYOUT);
        mLogoPreference = (Preference) getPreferenceScreen().findPreference(PAGE_LOGO);
        mLogoPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), PageLogoActivity.class);
                i.putExtra(PageFragment.EXTRA_ID, mPage.getId());
                startActivity(i);
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mNamePreference.setText(mPage.getName());
        mNamePreference.setSummary(mPage.getName());
        if(mPage.getName().equals(""))
            mNamePreference.setSummary(R.string.choose_name_summary);

        mDescPreference.setText(mPage.getDescription());
        mDescPreference.setSummary(mPage.getDescription());
        if(mPage.getDescription().equals(""))
            mDescPreference.setSummary(R.string.choose_description_summary);


        int index = mLayoutPreference.findIndexOfValue(mPage.settings.design.toString());
        mLayoutPreference.setValueIndex(index);
        mLayoutPreference.setSummary(mLayoutPreference.getEntry());

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);  // Set up a listener whenever a key changes

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);  // Unregister the listener whenever a key changes
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PAGE_LAYOUT)) {
            mPage.settings.design = Page.Design.getDesign(mLayoutPreference.getEntry().toString());
            mLayoutPreference.setSummary(mLayoutPreference.getEntry());
        }

        else if (key.equals(PAGE_NAME)) {
            if(mNamePreference.getText().equals("")) {
                Toast.makeText(getActivity(), R.string.no_page_name_toast, Toast.LENGTH_SHORT).show();
            }

            else {
                mPage.setName(mNamePreference.getText());
                mNamePreference.setSummary(mNamePreference.getText());
                if (mPage.getName().equals(""))
                    mNamePreference.setSummary(R.string.choose_name_summary);
            }
        }

        else if (key.equals(PAGE_DESC)) {
            mPage.setDescription(mDescPreference.getText());
            mDescPreference.setSummary(mDescPreference.getText());
            if(mPage.getDescription().equals(""))
                mDescPreference.setSummary(R.string.choose_description_summary);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                Intent intent = NavUtils.getParentActivityIntent(getActivity());
                intent.putExtra(PageFragment.EXTRA_ID, mPage.getId());

                NavUtils.navigateUpTo(getActivity(), intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Fragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ItemFragment.EXTRA_ID, id);

        PageSettingsFragment fragment = new PageSettingsFragment();
        fragment.setArguments(args);

        return (Fragment) fragment;
    }
}
