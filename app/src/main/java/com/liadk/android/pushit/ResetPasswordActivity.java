package com.liadk.android.pushit;

import android.support.v4.app.Fragment;

public class ResetPasswordActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ResetPasswordFragment();
    }
}
