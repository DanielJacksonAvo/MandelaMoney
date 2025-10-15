package com.example.mandelamoney.view.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IConfirmWithdrawView;
import com.example.mandelamoney.controller.WithdrawFundsController;

public class ConfirmWithdrawActivity extends AppCompatActivity implements IConfirmWithdrawView {
    private WithdrawFundsController controller;
    private ConstraintLayout loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_withdraw);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        controller = (WithdrawFundsController) DataShare.receive();
        connectToUI();
        controller.setConfirmWithdrawView(this);
        controller.handleLoadUsersUI();
    }
    private void connectToUI() {
        Button btnConfirm = findViewById(R.id.btn_confirmwithdraw);
        TextView btnCancel = findViewById(R.id.btn_confirmwithdraw_cancel);
        configureConfirmButton(btnConfirm);
        configureCancelButton(btnCancel);
        loadingSpinner = findViewById(R.id.confirm_withdraw_loading_spinner);

    }
    private void configureConfirmButton(Button btnConfirm) {
        btnConfirm.setOnClickListener((view) -> controller.handleConfirmWithdraw());
    }
    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCancelConfirmWithdraw());
    }
    @Override
    public void displayToUserName(String name) {
        TextView tbx = findViewById(R.id.txt_toname_confirmwithdraw);
        tbx.setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        TextView tbx = findViewById(R.id.txt_fromname_confirmwithdraw);
        tbx.setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_tonumber_confirmwithdraw);
        tbx.setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_fromnumber_confirmwithdraw);
        tbx.setText(number);
    }

    @Override
    public void displayAmount(double amount) {
        TextView tbx = findViewById(R.id.txt_amount_confirmwithdraw);
        @SuppressLint("DefaultLocale") String stringAmount = "R" + String.format("%.2f",amount);
        tbx.setText(stringAmount);
    }
    @Override
    public void showLoadingSpinner() {
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoadingSpinner() {
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void finishActivity() {
        finish();
    }


}
