package com.liadk.android.pushit;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.UUID;

public class ItemActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        UUID id = (UUID) getIntent().getSerializableExtra(ItemFragment.EXTRA_ID);
        return ItemFragment.newInstance(id);
    }
}
