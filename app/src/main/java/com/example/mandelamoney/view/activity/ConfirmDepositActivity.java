package com.example.mandelamoney.view.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DepositFundsController;
import com.example.mandelamoney.controller.MakePaymentController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IConfirmDepositView;

public class ConfirmDepositActivity extends AppCompatActivity implements IConfirmDepositView {
    private DepositFundsController controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_deposit);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        controller = (DepositFundsController) DataShare.receive();
        connectToUI();
        controller.setConfirmDepositView(this);
        controller.handleLoadUsersUI();
    }
    private void connectToUI() {
        Button btnConfirm = findViewById(R.id.btn_confirmdeposit);
        TextView btnCancel = findViewById(R.id.btn_confirmdeposit_cancel);
        configureConfirmButton(btnConfirm);
        configureCancelButton(btnCancel);
    }
    private void configureConfirmButton(Button btnConfirm) {
        btnConfirm.setOnClickListener((view) -> controller.handleConfirmDeposit());
    }
    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCancelConfirmDeposit());
    }
    @Override
    public void displayToUserName(String name) {
        TextView tbx = findViewById(R.id.txt_toname_confirmdeposit);
        tbx.setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        TextView tbx = findViewById(R.id.txt_fromname_confirmdeposit);
        tbx.setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_tonumber_confirmdeposit);
        tbx.setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_fromnumber_confirmdeposit);
        tbx.setText(number);
    }

    @Override
    public void displayAmount(double amount) {
        TextView tbx = findViewById(R.id.txt_amount_confirmdeposit);
        @SuppressLint("DefaultLocale") String stringAmount = "R" + String.format("%.2f",amount);
        tbx.setText(stringAmount);
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
