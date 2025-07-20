package com.example.mandelamoney;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ShowFailedActivity extends AppCompatActivity implements ITransactionStatusDisplayView {

    private int transactionId;
    private UserDetails fromUser;
    private UserDetails toUser;
    private TransactionDetails txnDetails;
    private String errorReason;
    private Button btn_close;
    private MakePaymentController makePaymentController;
    private RequestPaymentController requestPaymentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_failed);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get controller
        if (DataShare.receive() instanceof MakePaymentController) {
            makePaymentController = (MakePaymentController) DataShare.receive();
        } else if (DataShare.receive() instanceof RequestPaymentController) {
            requestPaymentController = (RequestPaymentController) DataShare.receive();
        }

        // Extract Intent data
        transactionId = getIntent().getIntExtra("TRANSACTION_ID", 0);
        errorReason = getIntent().getStringExtra("ERROR_REASON");

        if (errorReason != null) {
            displayErrorMessage(errorReason);
        }

        btn_close = findViewById(R.id.btn_generate_qr_failed);
        configureCloseButton(btn_close);
        if (transactionId != 0) {
            fetchDataAndPopulateUI();
        } else {
            Toast.makeText(this, "Transaction ID missing. Cannot load transaction details.", Toast.LENGTH_SHORT).show();
        }
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

    private void configureCloseButton(Button btnClose) {
        btnClose.setOnClickListener(v -> {
            startActivity(new Intent(ShowFailedActivity.this, DashboardActivity.class));
            finish();
        });
    }

    @Override
    public void displayToUserName(String name) {
        TextView tbx = findViewById(R.id.txt_toname_failed);
        tbx.setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        TextView tbx = findViewById(R.id.txt_fromname_failed);
        tbx.setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_tonumber_failed);
        tbx.setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_fromnumber_failed);
        tbx.setText(number);
    }

    @Override
    public void displayAmount(double amount) {
        TextView tbx = findViewById(R.id.txt_amount_failed);
        tbx.setText("R " + String.format("%.2f", amount));
    }

    public void displayErrorMessage(String reason) {
        TextView tbx = findViewById(R.id.txt_errormessage_failed); // Ensure this ID matches XML
        tbx.setText(reason);
    }
}
