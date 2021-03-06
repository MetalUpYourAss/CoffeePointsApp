package com.djaphar.coffeepointapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.djaphar.coffeepointapp.R;
import com.djaphar.coffeepointapp.SupportClasses.LocalDataClasses.User;
import com.djaphar.coffeepointapp.SupportClasses.OtherClasses.MyFragment;
import com.djaphar.coffeepointapp.SupportClasses.OtherClasses.UserChangeChecker;
import com.djaphar.coffeepointapp.ViewModels.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private TextView actionBarTitle;
    private MyFragment currentFragment;
    private UserChangeChecker userChangeChecker;
    private MainViewModel mainViewModel;
    private User user;
    private HashMap<String, String> authHeaderMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.action_bar);
            actionBarTitle = findViewById(R.id.action_bar_title);
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getUser().observe(this, user -> {
            if (user == null) {
                return;
            }
            this.user = user;
            authHeaderMap.put(getString(R.string.authorization_header), user.getToken());
            mainViewModel.requestUserProducts(authHeaderMap);
        });
        userChangeChecker = new UserChangeChecker(new Handler(), this);
    }

    @Override
    public void onBackPressed() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            currentFragment = (MyFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
        }

        if (currentFragment.everythingIsClosed()) {
            super.onBackPressed();
            return;
        }
        currentFragment.backWasPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userChangeChecker.startUserChangeCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        userChangeChecker.stopUserChangeCheck();
    }

    public void requestUser() {
        if (user == null) {
            return;
        }
        mainViewModel.requestUser(authHeaderMap, user.getUserHash());
    }

    public void setActionBarTitle(String title) {
        actionBarTitle.setText(title);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
