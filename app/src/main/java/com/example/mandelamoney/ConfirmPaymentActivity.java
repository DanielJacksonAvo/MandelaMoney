package com.example.mandelamoney;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConfirmPaymentActivity extends AppCompatActivity implements IConfirmPaymentView {

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
    }

    @Override
    public void displayToUserName(String name) {

    }

    @Override
    public void displayFromUserName(String name) {

    }

    @Override
    public void displayToUserNumber(String number) {

    }

    @Override
    public void displayFromUserNumber(String number) {

    }

    @Override
    public void displayToUserTransactionType(String type) {

    }

    @Override
    public void displayFromUserTransactionType(String type) {

    }

    @Override
    public void displayAmount(double amount) {

    }
}