package com.example.mandelamoney;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class RequestPayment_ShowQRActivity extends AppCompatActivity {

    private ImageView qrImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_payment_show_qractivity);

        // Set insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get reference to the ImageView
        qrImageView = findViewById(R.id.img_request_payment_qr);

        // Retrieve transactionId from intent
        String transactionId = getIntent().getStringExtra("transaction_id");

        if (transactionId != null && !transactionId.isEmpty()) {
            generateAndDisplayQRCode(transactionId);
        } else {
            // Optionally: show error or fallback QR
        }
    }

    private void generateAndDisplayQRCode(String transactionId) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                    transactionId,
                    BarcodeFormat.QR_CODE,
                    600, // width
                    600  // height
            );
            qrImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            // Optionally: show an error placeholder
        }
    }
}
