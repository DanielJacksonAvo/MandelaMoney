package com.example.mandelamoney.view.activity;

import android.annotation.SuppressLint;
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
import com.example.mandelamoney.controller.RequestPaymentController;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;

public class ShowSuccessActivity extends AppCompatActivity implements ITransactionStatusDisplayView {

    private int transactionId;

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

        Object controller = DataShare.receive();
        if (controller instanceof MakePaymentController) {
            MakePaymentController makePaymentController = (MakePaymentController) controller;
            makePaymentController.setTransactionStatusDisplayView(this);
            makePaymentController.setContext(this);
        } else if (controller instanceof RequestPaymentController) {
            RequestPaymentController requestPaymentController = (RequestPaymentController) controller;
        }

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", 0);

        if (transactionId != 0) {
            new Thread(this::fetchDataAndPopulateUI).start();
        }

        Button btnClose = findViewById(R.id.btn_generate_qr_success);
        btnClose.setOnClickListener(v -> {
            startActivity(new Intent(ShowSuccessActivity.this, DashboardActivity.class));
            finish();
        });
    }

    private void fetchDataAndPopulateUI() {
        final Transaction txnDetails = MySQLConnector.getTransactionDetailsFromProcedure(transactionId, this);

        if (txnDetails != null) {
            final User fromUser = MySQLConnector.getUserDetailsByEmail(txnDetails.getFromUser(), this);
            final User toUser = MySQLConnector.getUserDetailsByEmail(txnDetails.getToUser(), this);

            runOnUiThread(() -> {
                displayAmount(txnDetails.getAmount());
                if (fromUser != null) {
                    if (fromUser instanceof Student) {
                        displayFromUserName(((Student) fromUser).getStudentFullName());
                        displayFromUserNumber(((Student) fromUser).getStudentNumber());
                    } else if (fromUser instanceof Business) {
                        displayFromUserName(((Business) fromUser).getBusinessName());
                        displayFromUserNumber(((Business) fromUser).getBusinessVAT());
                    }
                }
                if (toUser != null) {
                    if (toUser instanceof Student) {
                        displayToUserName(((Student) toUser).getStudentFullName());
                        displayToUserNumber(((Student) toUser).getStudentNumber());
                    } else if (toUser instanceof Business) {
                        displayToUserName(((Business) toUser).getBusinessName());
                        displayToUserNumber(((Business) toUser).getBusinessVAT());
                    }
                }
            });
        }
    }

    @Override
    public void displayToUserName(String name) {
        ((TextView) findViewById(R.id.txt_toname_success)).setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        ((TextView) findViewById(R.id.txt_fromname_success)).setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        ((TextView) findViewById(R.id.txt_tonumber_success)).setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        ((TextView) findViewById(R.id.txt_fromnumber_success)).setText(number);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void displayAmount(double amount) {
        ((TextView) findViewById(R.id.txt_amount_success)).setText("R " + String.format("%.2f", amount));
    }
}