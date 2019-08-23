package com.liadk.android.pushit;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.UUID;

/**
 * Created by user on 10/08/2019.
 */
public class EditItemActivity extends SingleFragmentActivity {

    //
    // listener for pressing the Back button
    protected OnBackPressedListener onBackPressedListener;

    public interface OnBackPressedListener {
        void onBackPressed();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
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
        return EditItemFragment.newInstance(id);
    }

    @Override
    protected void onDestroy() {
        onBackPressedListener = null;
        super.onDestroy();
    }
}
