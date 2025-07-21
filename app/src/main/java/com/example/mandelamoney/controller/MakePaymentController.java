package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.camera.core.Camera; // Import Camera
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.mandelamoney.model.data.TransactionDetails;
import com.example.mandelamoney.model.data.User;
import com.example.mandelamoney.model.data.UserDetails;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.ImageUtils;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IConfirmPaymentView;
import com.example.mandelamoney.view.Iface.IScanQRView;
import com.example.mandelamoney.view.activity.ConfirmPaymentActivity;
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.MakePaymentScanQrActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;
import com.example.mandelamoney.view.activity.ShowSuccessActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MakePaymentController {

    private final Context context;
    private final IScanQRView scanQrView;
    private PreviewView previewView;
    private final ExecutorService cameraExecutor;

    private int transactionId;
    private float transactionAmount;
    private UserDetails fromUserDetails;
    private UserDetails toUserDetails;
    private IConfirmPaymentView confirmPaymentView;

    public MakePaymentController(Context context, IScanQRView scanQrView) {
        this.context = context;
        this.scanQrView = scanQrView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(context);
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    Image image = imageProxy.getImage();

                    if (image == null) {
                        imageProxy.close();
                        return;
                    }

                    try {
                        byte[] nv21Bytes = ImageUtils.yuv420_888toNv21(image);
                        if (nv21Bytes == null) {
                            Log.e("QRScan", "Failed to convert ImageProxy to NV21.");
                            return;
                        }

                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        byte[] rotatedNv21Bytes = ImageUtils.rotateNv21(nv21Bytes, image.getWidth(), image.getHeight(), rotationDegrees);

                        int rotatedWidth = image.getWidth();
                        int rotatedHeight = image.getHeight();
                        if (rotationDegrees == 90 || rotationDegrees == 270) {
                            rotatedWidth = image.getHeight();
                            rotatedHeight = image.getWidth();
                        }

                        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                                rotatedNv21Bytes,
                                rotatedWidth,
                                rotatedHeight,
                                0, 0,
                                rotatedWidth,
                                rotatedHeight,
                                false
                        );

                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = new MultiFormatReader().decode(bitmap);

                        transactionId = Integer.parseInt(result.getText().trim());
                        Log.d("QRScan", "Transaction ID detected: " + transactionId);

                    } catch (NotFoundException e) {
                        // Log.d("QRScan", "No QR code found in image.");
                    } catch (Exception e) {
                        Log.e("QRScan", "Unexpected error during image analysis: " + e.getMessage(), e);
                    } finally {
                        imageProxy.close();
                    }
                });

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle((MakePaymentScanQrActivity) context,
                        CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);

                // Set 2x zoom
                if (camera.getCameraInfo().getZoomState().getValue() != null) {
                    float maxZoom = camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();
                    float minZoom = camera.getCameraInfo().getZoomState().getValue().getMinZoomRatio();
                    float targetZoom = 2.0f;

                    // Ensure targetZoom is within the camera's capabilities
                    if (targetZoom >= minZoom && targetZoom <= maxZoom) {
                        camera.getCameraControl().setZoomRatio(targetZoom);
                        Log.d("CameraZoom", "Set zoom to: " + targetZoom + "x");
                    } else if (targetZoom > maxZoom) {
                        camera.getCameraControl().setZoomRatio(maxZoom);
                        Log.w("CameraZoom", "Requested zoom " + targetZoom + "x is greater than max zoom, setting to max: " + maxZoom + "x");
                    } else { // targetZoom < minZoom, though unlikely for 2x
                        camera.getCameraControl().setZoomRatio(minZoom);
                        Log.w("CameraZoom", "Requested zoom " + targetZoom + "x is less than min zoom, setting to min: " + minZoom + "x");
                    }
                }


            } catch (ExecutionException | InterruptedException e) {
                Log.e("Camera", "Failed to bind camera use cases", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void handleScanQR() {
        Log.d("DEBUG", "handleScanQR() triggered");

        User user = UserSession.getUser();
        if (user == null) {
            Log.e("DEBUG", "User session is null");
            Toast.makeText(context, "User session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        if (transactionId == 0) {
            Log.e("DEBUG", "transactionId = 0, no valid QR code detected yet.");
            Toast.makeText(context, "No QR code detected yet. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DEBUG", "Checking if transaction exists: " + transactionId);
        if (Boolean.TRUE.equals(MySQLConnector.transactionExists(context, transactionId))) {
            Log.i("TRANSACTION EXISTS", String.valueOf(transactionId));
            String email = user.getUserEmail();
            Log.i("FROM USER EMAIL", email);
            MySQLConnector.updateTransactionFromUser(context, transactionId, email);
            Log.i("MAKE PAYMENT CONTROLLER", "Attempt to updateTransactionFromUser complete");

            TransactionDetails tx = MySQLConnector.getTransactionDetailsFromProcedure(transactionId, context);
            this.transactionAmount = tx.getAmount();
            this.toUserDetails = MySQLConnector.getUserDetailsByEmail(tx.getToUser(), context);
            this.fromUserDetails = MySQLConnector.getUserDetailsByEmail(email, context);
            Log.i("TRANSACTION DETAILS CAPTURED", "Amount: " + transactionAmount + ", To: " + toUserDetails.getEmail());

            DataShare.send(this);
            context.startActivity(new Intent(context, ConfirmPaymentActivity.class));
        } else {
            Log.i("TRANSACTION ID", "TransactionId is not valid");
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
                        Log.e("MakePaymentController", "Transaction confirmed but failed in procedure.");
                        showFailScreen(true);
                    }
                } catch (Exception e) {
                    Log.e("MakePaymentController", "Error confirming transaction: " + e.getMessage());
                    showFailScreen(true);
                }
            } else {
                Log.w("MakePaymentController", "Insufficient funds for transaction " + transactionId);
                MySQLConnector.updateTransactionStatus(transactionId, "failed", context);
                showFailScreen(false);
            }

        } catch (Exception e) {
            Log.e("MakePaymentController", "Error checking sufficient funds: " + e.getMessage());
            showFailScreen(false);
        }
    }

    public void showFailScreen(boolean transactionFailed) {
        Intent intent = new Intent(context, ShowFailedActivity.class);
        intent.putExtra("TRANSACTION_ID", transactionId);
        if (!transactionFailed) {
            intent.putExtra("ERROR_REASON", "Insufficient Funds");
        } else {
            intent.putExtra("ERROR_REASON", "Transaction Failed or Reversed");
        }
        Log.d("MakePaymentController", "Navigating to ShowFailedActivity with reason: " + intent.getStringExtra("ERROR_REASON"));
        context.startActivity(intent);
        confirmPaymentView.finishActivity();
    }

    public void showSuccessScreen() {
        Intent intent = new Intent(context, ShowSuccessActivity.class);
        intent.putExtra("TRANSACTION_ID", transactionId);
        Log.d("MakePaymentController", "Navigating to ShowSuccessActivity with ID: " + transactionId);
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