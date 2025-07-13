package com.example.mandelamoney;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class RequestPayment_ShowQRActivity extends AppCompatActivity implements IShowQRCode_RequestPaymentView {
    private RequestPaymentController requestPaymentController;
    private ImageView qrImageView;
    private Integer transactionID;
    private TextView errorLabel;
    private int transactionIdNumeric;
    private Handler handler = new Handler();
    private Runnable statusChecker;
    private boolean isPolling = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_payment_show_qractivity);

        requestPaymentController = new RequestPaymentController(this, this);

        // Set insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get reference to the ImageView
        qrImageView = findViewById(R.id.img_request_payment_qr);
        errorLabel = findViewById(R.id.lbl_error_qr);
        TextView btnCancel = findViewById(R.id.btn_cancel_request_payment);
        configureCancelButton(btnCancel);


        // Retrieve transactionId from intent
        String transactionId = getIntent().getStringExtra("transaction_id");

        if (transactionId != null && !transactionId.isEmpty()) {
            generateAndDisplayQRCode(transactionId);
            transactionIdNumeric = Integer.parseInt(transactionId);
            requestPaymentController.startPollingStatus();

        } else {
            // Optionally: show error or fallback QR
        }
    }

    private void generateAndDisplayQRCode(String transactionId){
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(
                        transactionId,
                        BarcodeFormat.QR_CODE,
                        600, // width
                        600  // height
                );
                qrImageView.setImageBitmap(bitmap);

                // Hide error label on success
                if (errorLabel != null) {
                    errorLabel.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();

                // Show error label
                if (errorLabel != null) {
                    errorLabel.setVisibility(View.VISIBLE);
                }
            }
    }
    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> requestPaymentController.handleCancelButton());
    }

    @Override
    public void showError() {

    }

    @Override
    public void hideError() {

    }

    @Override
    public void finishActivity() {

    }
}
