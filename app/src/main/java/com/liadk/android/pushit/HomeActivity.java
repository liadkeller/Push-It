package com.liadk.android.pushit;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.app.SearchManager;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeActivity";

    private static final String KEY_STATUS_PREFERENCE = "statusPreference";
    private static final String KEY_SELECTED_ITEM = "selectedItem";

    private FirebaseAuth mAuth;
    private DatabaseManager mDatabaseManager;

    private boolean mUserStatus = false;
    private UUID mUserPageId;

    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseManager = DatabaseManager.get(this);

        Fragment defaultFragment = new ExploreFragment();
        loadFragment(defaultFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus();

        mBottomNavigationView = findViewById(R.id.bottomNavigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        mUserStatus = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_STATUS_PREFERENCE, false);
        onStatusUpdated();
    }

    private void updateNavState() {
        int actionId = 0;

        String className = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer).getClass().getSimpleName();

        switch(className) {
            case "PageFragment":
                actionId = R.id.bottom_nav_create;
                break;

            case "ExploreFragment":
                actionId = R.id.bottom_nav_explore;
                break;

            case "FollowFragment":
                actionId = R.id.bottom_nav_follow;
                break;

            case "ContentCreatorSettingsFragment":
            case "LoginSettingsFragment":
            case "SettingsFragment":
                actionId = R.id.bottom_nav_settings;
                break;
        }

        if(actionId != 0)
            mBottomNavigationView.setSelectedItemId(actionId);
    }

    // updates user status and triggers bottom nav inflation if necessary
    private void setUserStatus(boolean userStatus) {
        if(mUserStatus != userStatus) {
            mUserStatus = userStatus;

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(KEY_STATUS_PREFERENCE, mUserStatus)
                    .commit();

            onStatusUpdated();
        }
    }

    // update UI according to newly updated user status - inflate bottom nav from scratch
    private void onStatusUpdated() {
        mBottomNavigationView.getMenu().clear();

        if(mUserStatus)
            mBottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_creator);

        else
            mBottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);

        updateNavState();
    }

    // check the current user status and update if necessary
    public void updateUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null)
            setUserStatus(false);

        else {
            final String userId = user.getUid();

            mDatabaseManager.addUsersSingleEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    PushItUser user = dataSnapshot.child(userId).getValue(PushItUser.class);

                    boolean userStatus = (user != null) ? user.getStatus() : false;
                    setUserStatus(userStatus);

                    if(mUserStatus) {
                        if(user.getPageId() != null)
                            mUserPageId = UUID.fromString(user.getPageId());
                        else
                            setUserStatus(false);  // if page id is unavailable - updates to content-follower user (no page user)
                    }

                    if(user != null) {
                        PreferenceManager.getDefaultSharedPreferences(HomeActivity.this)
                                .edit()
                                .putString(SettingsFragment.KEY_EMAIL_PREFERENCE, user.getEmail())
                                .commit();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        if(item.getItemId() == R.id.bottom_nav_create && mUserPageId != null) {
            if(mUserStatus)
                fragment = PageFragment.newInstance(mUserPageId);
            else
                onStatusUpdated();
        }

        else if(item.getItemId() == R.id.bottom_nav_explore)
            fragment = new ExploreFragment();

        else if(item.getItemId() == R.id.bottom_nav_follow)
            fragment = new FollowFragment();

        else if(item.getItemId() == R.id.bottom_nav_settings) {
            if(mUserStatus)
                fragment = new ContentCreatorSettingsFragment();

            else
                fragment = new SettingsFragment();
        }


        if(fragment != null)
            loadFragment(fragment);

        return fragment != null;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Received a new search query: " + query);

            ((PageListFragment) fragment).loadQuery(query);
        }
    }
}