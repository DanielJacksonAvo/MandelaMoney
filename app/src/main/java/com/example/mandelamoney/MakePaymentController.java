package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
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

    // Remove volatile ImageProxy latestImage; and Object imageLock = new Object();
    // They are not needed with this approach as image.close() is handled in finally.

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
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, imageProxy -> { // Use imageProxy for clarity
                    Image image = imageProxy.getImage(); // Get the underlying Image object

                    if (image == null) {
                        imageProxy.close();
                        return;
                    }

                    try {
                        // 1. Convert ImageProxy to NV21 byte array, handling strides
                        byte[] nv21Bytes = ImageUtils.yuv420_888toNv21(image);
                        if (nv21Bytes == null) {
                            Log.e("QRScan", "Failed to convert ImageProxy to NV21.");
                            return; // Don't proceed if conversion failed
                        }

                        // 2. Get rotation degrees from ImageProxy and apply to NV21 data
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        byte[] rotatedNv21Bytes = ImageUtils.rotateNv21(nv21Bytes, image.getWidth(), image.getHeight(), rotationDegrees);

                        // Determine effective width and height after rotation for PlanarYUVLuminanceSource
                        int rotatedWidth = image.getWidth();
                        int rotatedHeight = image.getHeight();
                        if (rotationDegrees == 90 || rotationDegrees == 270) {
                            rotatedWidth = image.getHeight();
                            rotatedHeight = image.getWidth();
                        }

                        // 3. Create PlanarYUVLuminanceSource with the rotated data
                        // For NV21, PlanarYUVLuminanceSource expects the Y plane as its first argument
                        // and handles the UV component internally if the overall data is NV21.
                        // The 'dataWidth' and 'dataHeight' arguments are crucial here; they should
                        // be the dimensions of the *decoded* image after any rotation.
                        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                                rotatedNv21Bytes,
                                rotatedWidth, // Use rotated width
                                rotatedHeight, // Use rotated height
                                0, 0, // Crop x, y (no cropping in this example)
                                rotatedWidth, // Crop width
                                rotatedHeight, // Crop height
                                false // Don't invert (common for QR)
                        );

                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = new MultiFormatReader().decode(bitmap);

                        int scanned = Integer.parseInt(result.getText().trim());
                        transactionId = scanned;
                        Log.d("QRScan", "Transaction ID detected: " + transactionId);

                        // If you only want to process one QR code and then stop scanning
                        // consider adding a mechanism to stop the analyzer or unbind the use case
                        // once a QR code is successfully detected.
                        // Example: cameraProvider.unbind(analysis); // This stops the stream
                        // You'd need a way to re-bind if you want to scan again later.

                    } catch (NotFoundException e) {
                        // This is the common exception when no QR code is found in the image.
                        // Log.d("QRScan", "No QR code found in image.");
                    } catch (Exception e) {
                        Log.e("QRScan", "Unexpected error during image analysis: " + e.getMessage(), e);
                    } finally {
                        // ALWAYS close the ImageProxy when you are done with it to release the buffer.
                        imageProxy.close();
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
        Log.d("DEBUG", "handleScanQR() triggered");

        User user = UserSession.getUser();
        if (user == null) {
            Log.e("DEBUG", "User session is null");
            Toast.makeText(context, "User session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        if (transactionId == 0) { // Assuming 0 is an invalid transactionId
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