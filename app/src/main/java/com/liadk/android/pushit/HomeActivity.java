package com.liadk.android.pushit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    final UUID RANDOM_FAMILIAR_ID = UUID.fromString("48003ff7-f0a1-4556-be70-b42cf4298bc1"); // TODO DELETE

    BottomNavigationView mBottomNavigationView;
    boolean mUserStatus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBottomNavigationView = findViewById(R.id.bottomNavigation);
        mBottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);

        updateMenu();


        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Default Fragment TODO Depends on the fact Create is the default option
        Fragment defaultFragment = PageFragment.newInstance(RANDOM_FAMILIAR_ID);
        loadFragment(defaultFragment);
    }

    private void updateMenu() {
        Menu menu = mBottomNavigationView.getMenu();
        menu.findItem(R.id.bottom_nav_create).setVisible(mUserStatus);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        if(item.getItemId() == R.id.bottom_nav_create)
            fragment = PageFragment.newInstance(RANDOM_FAMILIAR_ID);

        else if(item.getItemId() == R.id.bottom_nav_explore)
            fragment = new ExploreFragment();

        else if(item.getItemId() == R.id.bottom_nav_settings)
            fragment = new SettingsFragment();


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



    public void setStatus(boolean status) {
        if(true) setContentCreator();
        else     setContentFollower();
    }

    public boolean getStatus() {
        return mUserStatus;
    }

    private void setContentCreator() {
        mUserStatus = true;
        updateMenu();
    }

    private void setContentFollower() {
        mUserStatus = false;
        updateMenu();
    }
}
