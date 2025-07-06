package com.example.mandelamoney;

import static android.icu.lang.UCharacter.toUpperCase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity implements IDashboardView {

    private DashboardController dashboardController;
    private TextView txtBalance, txtUserName;
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
        Intent intent = getIntent();
        User user = null;
        try {
            user = (User) intent.getSerializableExtra("user");
        } catch (Exception e) {
            Log.d("Dashboard","No user passed by intent.");
        }
        connectToUI();
        dashboardController = new DashboardController(this, this, user);
        dashboardController.handleLoadUserToUI();
    }

    private void connectToUI() {
        txtBalance = findViewById(R.id.txt_user_account_balance);
        txtUserName = findViewById(R.id.txt_user_name_dashboard);
        Button btnRequestPay = findViewById(R.id.btn_request_pay_dashboard);
        configureRequestPayButton(btnRequestPay);

    }

    @Override
    public void displayBalance(double balance) {
        String display = "R " + String.format(Locale.getDefault(), "%.2f", balance);
        txtBalance.setText(display);
    }

    @Override
    public void displayUserName(String name) {
        txtUserName.setText(toUpperCase(name));
    }

    private void configureRequestPayButton(Button btnRequestPay) {
        btnRequestPay.setOnClickListener((view) -> dashboardController.handleRequestPayment());

    }
}