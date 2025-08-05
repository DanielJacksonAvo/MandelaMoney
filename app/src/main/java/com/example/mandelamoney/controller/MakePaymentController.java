package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.ImageUtils;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.PaymentManager;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IConfirmPaymentView;
import com.example.mandelamoney.view.Iface.IScanQRView;
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;
import com.example.mandelamoney.view.activity.ConfirmPaymentActivity;
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.MainActivity;
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

    private Context context;
    private IScanQRView scanQrView;
    private PreviewView previewView;
    private final ExecutorService cameraExecutor;

    private int transactionId;
    private Transaction transaction;

    private IConfirmPaymentView confirmPaymentView;
    private ITransactionStatusDisplayView transactionStatusDisplayView;

    public MakePaymentController() {
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void setScanQrView(IScanQRView scanQrView) {
        this.scanQrView = scanQrView;
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

                if (camera.getCameraInfo().getZoomState().getValue() != null) {
                    float maxZoom = camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();
                    float minZoom = camera.getCameraInfo().getZoomState().getValue().getMinZoomRatio();
                    float targetZoom = 2.0f;

                    if (targetZoom >= minZoom && targetZoom <= maxZoom) {
                        camera.getCameraControl().setZoomRatio(targetZoom);
                        Log.d("CameraZoom", "Set zoom to: " + targetZoom + "x");
                    } else if (targetZoom > maxZoom) {
                        camera.getCameraControl().setZoomRatio(maxZoom);
                        Log.w("CameraZoom", "Requested zoom " + targetZoom + "x is greater than max zoom, setting to max: " + maxZoom + "x");
                    } else {
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
        scanQrView.showLoadingSpinner();
        PaymentManager.processTransaction(transactionId, context ,this::onScanQRSuccess, this::onScanQRError);
    }

    private void onScanQRSuccess(Transaction transaction) {
        this.transaction = transaction;
        scanQrView.hideLoadingSpinner();
        shutdown();
        DataShare.send(this);
        Intent intent = new Intent(context, ConfirmPaymentActivity.class);
        context.startActivity(intent);
    }

    private void onScanQRError(String error) {
        scanQrView.hideLoadingSpinner();
        scanQrView.showToast(error);
    }

    public void handleCancel() {
        DataShare.send(this);
        context.startActivity(new Intent(context, DashboardActivity.class));
        try {
            if (scanQrView != null) {
                scanQrView.finishActivity();
            }
            if (confirmPaymentView != null) {
                confirmPaymentView.finishActivity();
            }
            if (transactionStatusDisplayView != null) {
                transactionStatusDisplayView.finishActivity();
            }

        } catch (Exception ignored) {

        }
    }

    public void handleConfirmPayment() {
        confirmPaymentView.showLoadingSpinner();
        PaymentManager.confirmTransaction(transaction, context, this::showSuccessScreen, this::showFailScreen);

    }

    public void showFailScreen(String errorReason) {
        confirmPaymentView.hideLoadingSpinner();
        DataShare.send(this);
        context.startActivity(new Intent(context, ShowFailedActivity.class));
        try {
            if (scanQrView != null) {
                scanQrView.finishActivity();
            }
            if (confirmPaymentView != null) {
                confirmPaymentView.finishActivity();
            }
        } catch (Exception ignored) {
        }

    }

    public void showSuccessScreen() {
        confirmPaymentView.hideLoadingSpinner();
        DataShare.send(this);
        context.startActivity(new Intent(context, ShowSuccessActivity.class));
        try {
            if (scanQrView != null) {
                scanQrView.finishActivity();
            }
            if (confirmPaymentView != null) {
                confirmPaymentView.finishActivity();
            }
        } catch (Exception ignored) {
        }
    }

    public void shutdown() {
        cameraExecutor.shutdown();
    }

    public void setConfirmPaymentView(IConfirmPaymentView confirmPaymentView) {
        this.confirmPaymentView = confirmPaymentView;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void loadTransactionStatusData() {
        transactionStatusDisplayView.displayAmount(transaction.getAmount());
        if (transaction.getFromUserObj() != null) {
            if (transaction.getFromUserObj() instanceof Student) {
                transactionStatusDisplayView.displayFromUserName(((Student) transaction.getFromUserObj()).getStudentFullName());
                transactionStatusDisplayView.displayFromUserNumber(((Student) transaction.getFromUserObj()).getStudentNumber());
            } else if (transaction.getFromUserObj() instanceof Business) {
                transactionStatusDisplayView.displayFromUserName(((Business) transaction.getFromUserObj()).getBusinessName());
                transactionStatusDisplayView.displayFromUserNumber(((Business) transaction.getFromUserObj()).getBusinessVAT());
            }
        }
        if (transaction.getToUserObj() != null) {
            if (transaction.getToUserObj() instanceof Student) {
                transactionStatusDisplayView.displayToUserName(((Student) transaction.getToUserObj()).getStudentFullName());
                transactionStatusDisplayView.displayToUserNumber(((Student) transaction.getToUserObj()).getStudentNumber());
            } else if (transaction.getToUserObj() instanceof Business) {
                transactionStatusDisplayView.displayToUserName(((Business) transaction.getToUserObj()).getBusinessName());
                transactionStatusDisplayView.displayToUserNumber(((Business) transaction.getToUserObj()).getBusinessVAT());
            }
        }
    }

    public void loadConfirmPaymentData() {
        if (confirmPaymentView != null) {
            confirmPaymentView.displayAmount(transaction.getAmount());
            if (transaction.getFromUserObj() != null) {
                if (transaction.getFromUserObj() instanceof Student) {
                    confirmPaymentView.displayFromUserName(((Student) transaction.getFromUserObj()).getStudentFullName());
                    confirmPaymentView.displayFromUserNumber(((Student) transaction.getFromUserObj()).getStudentNumber());
                } else if (transaction.getFromUserObj() instanceof Business) {
                    confirmPaymentView.displayFromUserName(((Business) transaction.getFromUserObj()).getBusinessName());
                    confirmPaymentView.displayFromUserNumber(((Business) transaction.getFromUserObj()).getBusinessVAT());
                }
            }
            if (transaction.getToUserObj() != null) {
                if (transaction.getToUserObj() instanceof Student) {
                    confirmPaymentView.displayToUserName(((Student) transaction.getToUserObj()).getStudentFullName());
                    confirmPaymentView.displayToUserNumber(((Student) transaction.getToUserObj()).getStudentNumber());
                } else if (transaction.getToUserObj() instanceof Business) {
                    confirmPaymentView.displayToUserName(((Business) transaction.getToUserObj()).getBusinessName());
                    confirmPaymentView.displayToUserNumber(((Business) transaction.getToUserObj()).getBusinessVAT());
                }
            }
        }
    }

    public void setTransactionStatusDisplayView(ITransactionStatusDisplayView transactionStatusDisplayView) {
        this.transactionStatusDisplayView = transactionStatusDisplayView;
    }
}