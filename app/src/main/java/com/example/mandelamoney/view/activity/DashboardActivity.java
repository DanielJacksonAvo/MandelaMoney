package com.example.mandelamoney.view.activity;

import android.content.Intent;
import static java.security.AccessController.getContext;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.adapter.TransactionAdapter;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IDashboardView;
import com.example.mandelamoney.view.fragment.HomeDashboardFragment;
import com.example.mandelamoney.view.fragment.ProfileDashboardFragment;
import com.example.mandelamoney.view.fragment.SettingsDashboardFragment;
import com.example.mandelamoney.view.fragment.TransactionHistoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements IDashboardView {

    private DashboardController dashboardController;
    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment;
    private Fragment selectedFragmentExtra;
    private TextView txtUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dashboardController = new DashboardController(this, this);
        if (checkTablet()) {
            connectToUITablet();
            displayHome();
            displayUserName();
            displayTabletTransactionHistoryScreen();
        } else {
            connectToPhoneUI();
            displayHome();

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        dashboardController.handleLock();
    }

    private void displayUserName() {
        if (txtUserName != null) {
            dashboardController.handleLoadUserToUITablet();
        }
    }

    private void connectToUITablet() {
        txtUserName = findViewById(R.id.txt_user_name_dashboard);
        bottomNavigationView = findViewById(R.id.dashboardNavView);
        configureBottomNav();
    }

    private void connectToPhoneUI() {
        bottomNavigationView = findViewById(R.id.dashboardNavView);
        configureBottomNav();

    }

    private void configureBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            selectedFragment = null;

            int itemId = item.getItemId();
            dashboardController.handleSelection(itemId);
            return true;

        });
    }


    @Override
    public void displayHome() {
        selectedFragment = new HomeDashboardFragment();
        ((HomeDashboardFragment)selectedFragment).setController(dashboardController);
        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

    }


    @Override
    public void displaySettings() {
        selectedFragment = new SettingsDashboardFragment();
        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

    }

    @Override
    public void displayProfile() {
        selectedFragment = new ProfileDashboardFragment();
        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

    }

    @Override
    public void displayTabletTransactionHistoryScreen() {
        if (checkTablet()) {
            selectedFragmentExtra = new TransactionHistoryFragment(dashboardController);
            if (selectedFragmentExtra != null) {
                loadFragmentExtra(selectedFragmentExtra);
            }
        }
        else {
            selectedFragment = new TransactionHistoryFragment(dashboardController);
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
        }

    }

    public void displayUserNameTablet(String name) {
        txtUserName.setText(name);
    }

    public void loadFragmentExtra(Fragment fragment) {
        if (checkTablet() && findViewById(R.id.dashboardFrameExtra) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dashboardFrameExtra, fragment)
                    .commit();
        }

    }

    private void loadFragment(Fragment fragment) {
        if (findViewById(R.id.dashboardFrame) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dashboardFrame, fragment)
                    .commit();
        }

    }

    public boolean checkTablet() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }

}