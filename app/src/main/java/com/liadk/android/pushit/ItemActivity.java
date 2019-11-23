package com.liadk.android.pushit;

import android.support.v4.app.Fragment;

import java.util.UUID;

public class ItemActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        UUID id = (UUID) getIntent().getSerializableExtra(ItemFragment.EXTRA_ID);
        return ItemFragment.newInstance(id);
    }
}
