package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MakePaymentController {

    private final Context context;
    private final IScanQRView scanQrView;
    private PreviewView previewView;
    private final ExecutorService cameraExecutor;
    private volatile ImageProxy latestImage;
    private final Object imageLock = new Object();

    private int transactionId;
    private float transactionAmount;
    private UserDetails fromUserDetails;
    private UserDetails toUserDetails;
    private IConfirmPaymentView confirmPaymentView;

    public MakePaymentController(Context context, IScanQRView scanQrView ) {
        this.context = context;
        this.scanQrView = scanQrView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();

    }

    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(context);
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, image -> {
                    synchronized (imageLock) {
                        if (latestImage != null) latestImage.close();
                        latestImage = image;
                    }

                    try {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);

                        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                                bytes, image.getWidth(), image.getHeight(),
                                0, 0, image.getWidth(), image.getHeight(), false);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = new MultiFormatReader().decode(bitmap);

                        int scanned = Integer.parseInt(result.getText().trim());
                        transactionId = scanned;
                        Log.d("QRScan", "Transaction ID detected: " + transactionId);

                    } catch (Exception e) {
                        // Ignore if no QR found – normal for continuous scan
                    } finally {
                        image.close();
                    }
                });


                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((MakePaymentScanQrActivity) context,
                        CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("Camera", "Failed to bind camera use cases", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void handleScanQR() {
        User user = UserSession.getUser();
        if (user == null) {
            Toast.makeText(context, "User session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        if (transactionId == 0) {
            Toast.makeText(context, "No QR code detected yet. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Boolean.TRUE.equals(MySQLConnector.transactionExists(context, transactionId))) {
            Log.i("TRANSACTION EXISTS","f"+(transactionId));
            String email = user.getUserEmail();
            Log.i("FROM USER EMAIL",email);
            MySQLConnector.updateTransactionFromUser(context, transactionId, email);
            Log.i("MAKE PAYMENT CONTROLLER", "Attempt to updateTransactionFromUser complete");
            TransactionDetails tx = MySQLConnector.getTransactionDetailsFromProcedure(transactionId, context);
            this.transactionAmount = tx.getAmount();
            this.toUserDetails = MySQLConnector.getUserDetailsByEmail(tx.getToUser(), context);
            this.fromUserDetails = MySQLConnector.getUserDetailsByEmail(email, context);
            Log.i("TRANSACTION DETAILS CAPTURED", "TransactionAmount: "+transactionAmount + " toUserEmail: "+ toUserDetails.getEmail()+" fromUserDetails: "+ fromUserDetails.getEmail());

            DataShare.send(this);
            context.startActivity(new Intent(context, ConfirmPaymentActivity.class));
        } else {
            Toast.makeText(context, "Invalid Transaction ID", Toast.LENGTH_SHORT).show();
        }

    }

    public void handleCancel() {
        DataShare.send(this);
        context.startActivity(new Intent(context, DashboardActivity.class));
        scanQrView.finishActivity();
    }

    public void handleConfirmPayment() {
        boolean transactionSuccess;
        boolean sufficientFunds;

        try {
            sufficientFunds = MySQLConnector.hasSufficientFunds(fromUserDetails.getEmail(), transactionId, context);

            if (sufficientFunds) {
                try {
                    transactionSuccess = MySQLConnector.confirmTransaction(fromUserDetails.getEmail(), transactionId, context);

                    if (transactionSuccess) {
                        showSuccessScreen();
                    } else {
                        showFailScreen(true);
                    }
                } catch (Exception e) {
                    Log.e("MakePaymentController", "Error confirming transaction: " + e.getMessage());
                    showFailScreen(true);
                }
            } else {
                showFailScreen(false);
            }

        } catch (Exception e) {
            Log.e("MakePaymentController", "Error checking sufficient funds: " + e.getMessage());
            showFailScreen(false);
        }
    }

    public void showFailScreen(boolean transactionFailed) {
        Intent intent = new Intent(context, ShowFailedActivity.class);
        if (!transactionFailed) {
            intent.putExtra("ERROR_REASON", "Insufficient Funds");
        } else {
            intent.putExtra("ERROR_REASON", "Transaction Failed or Reversed");
        }
        context.startActivity(intent);
        confirmPaymentView.finishActivity();
    }

    public void showSuccessScreen() {
        Intent intent = new Intent(context, ShowSuccessActivity.class);
        intent.putExtra("TRANSACTION_ID", transactionId);
        context.startActivity(intent);
        confirmPaymentView.finishActivity();
    }

    public void shutdown() {
        cameraExecutor.shutdown();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void handleLoadUsersUI() {
        confirmPaymentView.displayAmount(transactionAmount);
        confirmPaymentView.displayFromUserName(fromUserDetails.getFirstName() + " " + fromUserDetails.getLastName());
        confirmPaymentView.displayFromUserNumber(fromUserDetails.getNumber());
        confirmPaymentView.displayToUserName(toUserDetails.getFirstName() + " " + toUserDetails.getLastName());
        confirmPaymentView.displayToUserNumber(toUserDetails.getNumber());
    }

    public void setConfirmPaymentView(IConfirmPaymentView confirmPaymentView) {
        this.confirmPaymentView = confirmPaymentView;
    }
}
