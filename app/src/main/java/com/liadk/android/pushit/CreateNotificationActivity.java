package com.liadk.android.pushit;

import android.support.v4.app.Fragment;

import java.util.UUID;

public class CreateNotificationActivity extends SingleFragmentActivity {

    //
    // listener for pressing the Back button
    protected EditItemActivity.OnBackPressedListener onBackPressedListener;  // We use EditItemActivity interface and not define our own

    public void setOnBackPressedListener(EditItemActivity.OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    public void onBackPressed() {
        if (onBackPressedListener != null)
            onBackPressedListener.onBackPressed();
        else
            super.onBackPressed();
    }


    //
    // createFragment and lifecycle methods implementation
    @Override
    protected Fragment createFragment() {
        UUID id = (UUID) getIntent().getSerializableExtra(ItemFragment.EXTRA_ID);
        return CreateNotificationFragment.newInstance(id);
    }
}
