package com.liadk.android.pushit;

import android.support.v4.app.Fragment;

public class UpdateEmailActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new UpdateEmailFragment();
    }
}
