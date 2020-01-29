package com.rgbtape.app;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    public static final String HOME = "Home";
    public static final String CUSTOM_EFFECTS = "Custom Effects";
    public static final String ALARMS = "Alarms";

    private Fragment homeFragment;
    private Fragment alarmsFragment;
    private Fragment customEffectsFragment;

    private FragmentManager fragmentManager;

    private String currentFragment;

    public ConnectionHandler connectionHandler;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                            new HomeFragment()).commit();

                    switchFragments(HOME);
                    return true;
                case R.id.navigation_alarms:
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                            new AlarmsFragment()).commit();

                    switchFragments(ALARMS);
                    return true;
                case R.id.navigation_custom_effects:
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                            new CustomEffectsFragment()).commit();
                    switchFragments(CUSTOM_EFFECTS);
                    return true;
            }
            return false;
        }
    };

    private void switchFragments(String newFragment){
        if (fragmentManager != null && !newFragment.equals(currentFragment)) {
            switch (currentFragment) {
                case HOME:
                    fragmentManager.beginTransaction()
                            .hide(homeFragment).commit();
                    break;
                case ALARMS:
                    fragmentManager.beginTransaction()
                            .hide(alarmsFragment).commit();
                    break;
                case CUSTOM_EFFECTS:
                    fragmentManager.beginTransaction()
                            .hide(customEffectsFragment).commit();
                    break;
            }

            currentFragment = newFragment;

            switch (newFragment){
                case HOME:
                    fragmentManager.beginTransaction()
                            .show(homeFragment).commit();
                    break;
                case ALARMS:
                    fragmentManager.beginTransaction()
                            .show(alarmsFragment).commit();
                    break;
                case CUSTOM_EFFECTS:
                    fragmentManager.beginTransaction()
                            .show(customEffectsFragment).commit();
                    break;
            }
        }
    }

    private void initFragments(ConnectionHandler connectionHandler){
        homeFragment = new HomeFragment(connectionHandler);
        alarmsFragment = new AlarmsFragment(connectionHandler);
        customEffectsFragment = new CustomEffectsFragment();

        if (fragmentManager != null){
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, homeFragment)
                    .add(R.id.fragment_container, alarmsFragment)
                    .add(R.id.fragment_container, customEffectsFragment)
                    .hide(customEffectsFragment)
                    .hide(alarmsFragment)
                    .show(homeFragment)
                    .commit();
            currentFragment = HOME;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        connectionHandler = new ConnectionHandler();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setSelectedItemId(R.id.navigation_home);

        //Instantiate all fragments
        initFragments(connectionHandler);




        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
