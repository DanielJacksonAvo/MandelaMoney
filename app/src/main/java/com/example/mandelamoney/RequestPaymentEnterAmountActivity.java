package com.example.mandelamoney;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RequestPaymentEnterAmountActivity extends AppCompatActivity implements IEnterAmountRequestPaymentView {

    private RequestPaymentController requestPaymentController;
    private TextView txtErrorMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_payment_enter_amount);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        requestPaymentController = new RequestPaymentController(this, this);
        connectToUI();
    }

    private void connectToUI() {
        Button btnGenerateQRButton = findViewById(R.id.btn_generate_qr_request_payment);
        EditText tbxPaymentAmount = findViewById(R.id.tbx_amount_request_payment);
        TextView btnCancel = findViewById(R.id.btn_cancel_request_payment);
        txtErrorMessage = findViewById(R.id.txt_error_request_payment);
        configureGenerateQRButton(btnGenerateQRButton, tbxPaymentAmount);
        configureCancelButton(btnCancel);


    }

    private void configureGenerateQRButton(Button btnGenerateQRButton, EditText tbxPaymentAmount) {
        btnGenerateQRButton.setOnClickListener((view) -> requestPaymentController.handleGenerateQR(String.valueOf(tbxPaymentAmount.getText())));
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> requestPaymentController.handleCancelButton());
    }

    @Override
    public void showError(String message) {
        txtErrorMessage.setText(message);
        txtErrorMessage.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void hideError() {
        txtErrorMessage.setVisibility(TextView.GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }
}