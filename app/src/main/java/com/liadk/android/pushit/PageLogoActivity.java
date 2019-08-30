package com.liadk.android.pushit;

import android.support.v4.app.Fragment;

import java.util.UUID;

public class PageLogoActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        UUID id = (UUID) getIntent().getSerializableExtra(PageFragment.EXTRA_ID);
        return PageLogoFragment.newInstance(id);
    }
}
