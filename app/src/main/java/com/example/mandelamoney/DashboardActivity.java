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

        connectToUI();

        User currentUser = UserSession.getUser();

        if (currentUser == null) {
            Log.e("DashboardActivity", "User session is null. Returning to login.");
            // Optionally navigate to login activity:
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        dashboardController = new DashboardController(this, this);
        dashboardController.handleLoadUserToUI();
    }


    private void connectToUI() {
        txtBalance = findViewById(R.id.txt_user_account_balance);
        txtUserName = findViewById(R.id.txt_user_name_dashboard);
        Button btnRequestPay = findViewById(R.id.btn_request_pay_dashboard);
        configureRequestPayButton(btnRequestPay);
        Button btnPayNow = findViewById(R.id.btn_pay_now);
        configurePayNowButton(btnPayNow);

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
    private void configurePayNowButton(Button btnPayNow){
        btnPayNow.setOnClickListener((view)->dashboardController.handleMakePayment());
    }
}