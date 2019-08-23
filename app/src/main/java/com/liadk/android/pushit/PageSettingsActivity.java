package com.liadk.android.pushit;

import android.support.v4.app.Fragment;

import java.util.UUID;

public class PageSettingsActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        UUID id = (UUID) getIntent().getSerializableExtra(PageFragment.EXTRA_ID);
        return PageSettingsFragment.newInstance(id);
    }
}
