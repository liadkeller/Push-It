package com.liadk.android.pushit;

import android.support.v4.app.Fragment;

public class PageActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PageFragment();
    }
}
