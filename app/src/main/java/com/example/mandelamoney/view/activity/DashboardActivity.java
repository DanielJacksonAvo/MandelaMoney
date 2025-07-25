package com.example.mandelamoney.view.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IDashboardView;
import com.example.mandelamoney.view.fragment.HomeDashboardFragment;
import com.example.mandelamoney.view.fragment.ProfileDashboardFragment;
import com.example.mandelamoney.view.fragment.SettingsDashboardFragment;
import com.example.mandelamoney.view.fragment.TransactionHistoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity implements IDashboardView {

    private DashboardController dashboardController;
    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment;
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
        } else {
            connectToUI();
            displayHome();

        }

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

    private void connectToUI() {
        bottomNavigationView = findViewById(R.id.dashboardNavView);
        configureBottomNav();

    }

    private void configureBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectedFragment = null;

                int itemId = item.getItemId();
                dashboardController.handleSelection(itemId);
                return true;

            }
        });
    }


    @Override
    public void displayHome() {
        selectedFragment = new HomeDashboardFragment(dashboardController);
        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
    }

    @Override
    public void displayLock() {

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
    public void displayTransactionHistoryScreen() {
        selectedFragment = new TransactionHistoryFragment(dashboardController);
        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
    }

    public void displayUserNameTablet(String name) {
        txtUserName.setText(name);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dashboardFrame, fragment)
                .commit();
    }

    public boolean checkTablet() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }
}