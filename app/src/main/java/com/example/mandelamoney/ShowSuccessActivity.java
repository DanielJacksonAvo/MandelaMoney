package com.example.mandelamoney;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ShowSuccessActivity extends AppCompatActivity {

    private TextView fromName, fromNumber, toName, toNumber, amount;

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

        initViews();
        loadTransactionData();
    }

    private void initViews() {
        fromName = findViewById(R.id.txt_fromname_success);
        fromNumber = findViewById(R.id.txt_fromnumber_success);
        toName = findViewById(R.id.txt_toname_success);
        toNumber = findViewById(R.id.txt_tonumber_success);
        amount = findViewById(R.id.txt_amount_success);
    }

    private void loadTransactionData() {
        int transactionId = getIntent().getIntExtra("transaction_id", -1);
        if (transactionId == -1) return;

        TransactionDetails details = MySQLConnector.getTransactionDetailsFromProcedure(transactionId, this);
        if (details == null) return;

        UserDetails fromUser = MySQLConnector.getUserDetailsByEmail(details.getFromUser(), this);
        UserDetails toUser = MySQLConnector.getUserDetailsByEmail(details.getToUser(), this);

        if (fromUser != null) {
            fromName.setText(fromUser.getFullName());
            fromNumber.setText(fromUser.getNumber());
        }
        if (toUser != null) {
            toName.setText(toUser.getFullName());
            toNumber.setText(toUser.getNumber());
        }

        amount.setText("R" + String.format("%.2f", details.getAmount()));
    }
}
