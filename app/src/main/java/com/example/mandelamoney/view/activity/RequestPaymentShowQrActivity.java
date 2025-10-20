package com.example.mandelamoney.view.activity;

import static android.widget.Toast.LENGTH_LONG;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.RequestPaymentController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IShowQRCodeRequestPaymentView;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class RequestPaymentShowQrActivity extends AppCompatActivity implements IShowQRCodeRequestPaymentView {
    private RequestPaymentController requestPaymentController;
    private ImageView qrImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_payment_show_qr);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        requestPaymentController = (RequestPaymentController) DataShare.receive();
        requestPaymentController.setContext(this);
        connectToUi();
        requestPaymentController.setShowQRCodeRequestPaymentView(this);
        requestPaymentController.generateQR();
        requestPaymentController.startPollingStatus();
    }

    private void connectToUi() {
        qrImageView = findViewById(R.id.img_request_payment_qr);
        TextView btnCancel = findViewById(R.id.btn_cancel_request_payment);
        configureCancelButton(btnCancel);
    }

    public void displayQR(Bitmap bitmap){
        try {
            qrImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            displayToast("Error Displaying QR Code");
        }
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> requestPaymentController.handleCancelButton());
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(this, message, LENGTH_LONG).show();
    }

    @Override public void finishActivity() {finish();}
}
