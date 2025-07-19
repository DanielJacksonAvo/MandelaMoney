package com.example.mandelamoney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ShowSuccessActivity extends AppCompatActivity implements ITransactionStatusDisplayView {

    private int transactionId;
    private UserDetails fromUser;
    private UserDetails toUser;
    private TransactionDetails txnDetails;
    private Button btnClose;
    private MakePaymentController makePaymentController;
    private RequestPaymentController requestPaymentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (DataShare.receive() instanceof MakePaymentController) {
            makePaymentController = (MakePaymentController) DataShare.receive();
        } else if (DataShare.receive() instanceof RequestPaymentController) {
            requestPaymentController = (RequestPaymentController) DataShare.receive();
        }

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", 0);

        if (transactionId != 0) {
            fetchDataAndPopulateUI();
        }

        btnClose = findViewById(R.id.btn_generate_qr_success);
        btnClose.setOnClickListener(v -> {
            startActivity(new Intent(ShowSuccessActivity.this, DashboardActivity.class));
            finish();
        });
    }

    private void fetchDataAndPopulateUI() {
        txnDetails = MySQLConnector.getTransactionDetailsFromProcedure(transactionId, this);

        if (txnDetails != null) {
            fromUser = MySQLConnector.getUserDetailsByEmail(txnDetails.getFromUser(), this);
            toUser = MySQLConnector.getUserDetailsByEmail(txnDetails.getToUser(), this);

            displayAmount(txnDetails.getAmount());
            displayFromUserName(fromUser.getFirstName() + " " + fromUser.getLastName());
            displayFromUserNumber(fromUser.getNumber());
            displayToUserName(toUser.getFirstName() + " " + toUser.getLastName());
            displayToUserNumber(toUser.getNumber());
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

    @Override
    public void displayAmount(double amount) {
        ((TextView) findViewById(R.id.txt_amount_success)).setText("R " + String.format("%.2f", amount));
    }
}
