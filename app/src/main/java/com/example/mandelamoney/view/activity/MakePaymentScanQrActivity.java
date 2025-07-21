package com.example.mandelamoney.view.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.MakePaymentController;
import com.example.mandelamoney.view.Iface.IScanQRView;

public class MakePaymentScanQrActivity extends AppCompatActivity implements IScanQRView {

    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private MakePaymentController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_payment_scan_qr);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PreviewView previewView = findViewById(R.id.img_request_payment_qr);
        controller = new MakePaymentController(this, this);
        controller.setPreviewView(previewView);

        checkCameraPermission();
        connectToUI();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            controller.startCamera();
        }
    }

    private void connectToUI() {
        Button btnScan = findViewById(R.id.btn_scan_qr);
        TextView btnCancel = findViewById(R.id.btn_cancel_scan_qr);
        btnScan.setOnClickListener(v -> {
            Log.d("DEBUG", "Scan button clicked — transactionId: " + controller.getTransactionId());
            controller.handleScanQR();
        });

        btnCancel.setOnClickListener(v -> controller.handleCancel());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            controller.startCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void showErrorMessage() {}
    @Override public void hideErrorMessage() {}
    @Override public void finishActivity() {}
}

