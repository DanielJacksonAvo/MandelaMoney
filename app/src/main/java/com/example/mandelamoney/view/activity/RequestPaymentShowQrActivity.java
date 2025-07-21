package com.example.mandelamoney.view.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.RequestPaymentController;
import com.example.mandelamoney.view.Iface.IShowQRCodeRequestPaymentView;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class RequestPaymentShowQrActivity extends AppCompatActivity implements IShowQRCodeRequestPaymentView {
    private RequestPaymentController requestPaymentController;
    private ImageView qrImageView;
    private TextView errorLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_payment_show_qr);

        requestPaymentController = new RequestPaymentController(this, this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        qrImageView = findViewById(R.id.img_request_payment_qr);
        errorLabel = findViewById(R.id.lbl_error_qr);
        TextView btnCancel = findViewById(R.id.btn_cancel_request_payment);
        configureCancelButton(btnCancel);

        String transactionId = getIntent().getStringExtra("transaction_id");
        if (transactionId != null && !transactionId.isEmpty()) {
            int transactionIdNumeric = Integer.parseInt(transactionId);
            generateAndDisplayQRCode(transactionId);
            requestPaymentController.startPollingStatus(transactionIdNumeric);
        }
    }

    private void generateAndDisplayQRCode(String transactionId){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(transactionId, BarcodeFormat.QR_CODE, 600, 600);
            qrImageView.setImageBitmap(bitmap);
            errorLabel.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setVisibility(View.VISIBLE);
        }
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> requestPaymentController.handleCancelButton());
    }

    @Override public void showError() {}
    @Override public void hideError() {}
    @Override public void finishActivity() {}
}
