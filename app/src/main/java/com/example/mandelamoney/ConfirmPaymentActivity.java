package com.example.mandelamoney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConfirmPaymentActivity extends AppCompatActivity implements IConfirmPaymentView {

    private MakePaymentController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        connectToUI();
    }

    private void connectToUI() {
        Button btnConfirm = findViewById(R.id.btn_confirmpayment);
        TextView btnCancel = findViewById(R.id.btn_confirmpayment_cancel);
        configureConfirmButton(btnConfirm);
        configureCancelButton(btnCancel);
    }

    private void configureConfirmButton(Button btnConfirm) {
        btnConfirm.setOnClickListener((view) -> {
            controller.handleConfirmPayment();
        });
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> {
            controller.handleCancel();
        });
    }

    @Override
    public void displayToUserName(String name) {
        TextView tbx = findViewById(R.id.txt_toname_confirmpayment);
        tbx.setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        TextView tbx = findViewById(R.id.txt_fromname_confirmpayment);
        tbx.setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_tonumber_confirmpayment);
        tbx.setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_fromnumber_confirmpayment);
        tbx.setText(number);
    }

    @Override
    public void displayToUserTransactionType(String type) {
        TextView tbx = findViewById(R.id.txt_transactiontypeto_confirmpayment);
        tbx.setText(type);
    }

    @Override
    public void displayFromUserTransactionType(String type) {
        TextView tbx = findViewById(R.id.txt_transactiontypefrom_confirmpayment);
        tbx.setText(type);
    }

    @Override
    public void displayAmount(double amount) {
        TextView tbx = findViewById(R.id.txt_amount_confirmpayment);
        tbx.setText(String.valueOf(amount));    }


}