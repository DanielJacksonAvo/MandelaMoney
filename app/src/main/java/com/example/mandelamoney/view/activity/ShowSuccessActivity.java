package com.example.mandelamoney.view.activity;

import android.content.Intent;
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
import com.example.mandelamoney.controller.MakePaymentController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;

public class ShowSuccessActivity extends AppCompatActivity implements ITransactionStatusDisplayView {

    private TextView txtToname, txtFromname, txtTonumber, txtFromnumber, txtAmount;
    private MakePaymentController makePaymentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_success);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Object obj = DataShare.receive();
        if (obj instanceof MakePaymentController) {
            makePaymentController = (MakePaymentController) obj;
            makePaymentController.setTransactionStatusDisplayView(this);
            makePaymentController.setContext(this);
        }
        connectToUi();
        makePaymentController.loadTransactionStatusData();



    }

    private void connectToUi() {
        txtToname = findViewById(R.id.txt_toname_success);
        txtFromname = findViewById(R.id.txt_fromname_success);
        txtTonumber = findViewById(R.id.txt_tonumber_success);
        txtFromnumber = findViewById(R.id.txt_fromnumber_success);
        txtAmount = findViewById(R.id.txt_amount_success);
        Button btnClose = findViewById(R.id.btn_generate_qr_success);
        configureCloseButton(btnClose);

    }

    @Override
    public void displayToUserName(String name) {
        txtToname.setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        txtFromname.setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        txtTonumber.setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        txtFromnumber.setText(number);
    }

    @Override
    public void displayAmount(double amount) {
        txtAmount.setText("R " + String.format("%.2f", amount));
    }

    @Override
    public void finishActivity() {
        finish();
    }

    private void configureCloseButton(Button btnClose) {
        btnClose.setOnClickListener(v -> {
            makePaymentController.handleCancel();
        });
    }
}