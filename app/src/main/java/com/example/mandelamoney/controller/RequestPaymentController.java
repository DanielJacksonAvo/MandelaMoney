package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Looper;
import android.os.Handler;
import android.util.Log; // Import Log for debugging

import androidx.core.content.ContextCompat; // For getting main executor

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.PaymentManager;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEnterAmountRequestPaymentView;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;
import com.example.mandelamoney.view.Iface.IShowQRCodeRequestPaymentView; // Not directly used but good to keep
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;
import com.example.mandelamoney.view.activity.ConfirmPaymentActivity;
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.RequestPaymentShowQrActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;
import com.example.mandelamoney.view.activity.ShowSuccessActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.concurrent.ExecutorService; // New import
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RequestPaymentController {
    private Context context;
    private int transactionIdNumeric;
    private Transaction transaction;
    private RequestPaymentShowQrActivity requestPaymentShowQrActivity;
    private IEnterAmountRequestPaymentView enterAmountRequestPaymentView;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private ScheduledFuture<?> pollingHandle;
    private Runnable statusChecker;

    private final ExecutorService requestExecutor;
    private ITransactionStatusDisplayView transactionStatusDisplayView;

    public RequestPaymentController() {
        requestExecutor = Executors.newSingleThreadExecutor();
    }


    public void handleGenerateQR(String samount) {
        if (!ValidateInput.checkValidAmount(samount)) {
            enterAmountRequestPaymentView.showError("Invalid Amount");
            return;
        }
        float amount = Float.parseFloat(samount);
        amount = (float) Math.ceil(amount * 100) / 100;
        PaymentManager.createTransaction(amount, context, this::onCreateTransactionSuccess, this::onCreateTransactionFailure);
    }

    private void onCreateTransactionSuccess(Transaction transaction) {
        this.transaction = transaction;
        enterAmountRequestPaymentView.hideError();
        enterAmountRequestPaymentView.hideLoadingSpinner();
        DataShare.send(this);
        Intent intent = new Intent(context, RequestPaymentShowQrActivity.class);
        context.startActivity(intent);
    }

    private void onCreateTransactionFailure(String error) {
        enterAmountRequestPaymentView.hideLoadingSpinner();
        enterAmountRequestPaymentView.showError(error);
    }
    public void startPollingStatus() {
        if (pollingHandle != null && !pollingHandle.isDone()) {
            Log.d("RequestPaymentController", "Polling already active or pending.");
            return;
        }
        Log.d("RequestPaymentController", "Starting polling for transaction ID: " + transaction.getId());


        statusChecker = () -> {
            try {
                String status = MySQLConnector.getTransactionStatus(Integer.parseInt(transaction.getId()), context);
                Log.d("RequestPaymentController", "Polling status for " + transaction.getId() + ": " + status);
                mainThreadHandler.post(() -> {
                    if ("success".equalsIgnoreCase(status)) {
                        stopPolling();
                        Log.d("RequestPaymentController", "Transaction " + transaction.getId() + " succeeded. Navigating to success screen.");
                        Intent intent = new Intent(context, ShowSuccessActivity.class);
                        DataShare.send(this);
                        context.startActivity(intent);
                    } else if ("failed".equalsIgnoreCase(status)) {
                        stopPolling();
                        Log.d("RequestPaymentController", "Transaction " + transaction.getId() + " failed. Navigating to failed screen.");
                        Intent intent = new Intent(context, ShowFailedActivity.class);
                        DataShare.send(this);
                        context.startActivity(intent);
                    }
                });
            } catch (Exception e) {
                requestPaymentShowQrActivity.displayToast("Confirmation Display Error");
            }
        };
        pollingHandle = scheduler.scheduleAtFixedRate(statusChecker, 0, 1, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        if (pollingHandle != null) {
            pollingHandle.cancel(false);
            pollingHandle = null;
            Log.d("RequestPaymentController", "Polling stopped.");
        }
    }


    public void setContext(Context context) {
        this.context = context;
    }

    public void setEnterAmountRequestPaymentView(IEnterAmountRequestPaymentView enterAmountRequestPaymentView) {
        this.enterAmountRequestPaymentView = enterAmountRequestPaymentView;
    }

    public void handleCancelButton() {
        stopPolling();
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
    }

    public void cleanup() {
        stopPolling();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ie) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        // Shutdown the new requestExecutor
        if (requestExecutor != null && !requestExecutor.isShutdown()) {
            requestExecutor.shutdown();
            try {
                if (!requestExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    requestExecutor.shutdownNow();
                }
            } catch (InterruptedException ie) {
                requestExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        mainThreadHandler.removeCallbacksAndMessages(null);
        Log.d("RequestPaymentController", "Controller cleaned up.");
    }

    public void setShowQRCodeRequestPaymentView(RequestPaymentShowQrActivity requestPaymentShowQrActivity) {
        this.requestPaymentShowQrActivity = requestPaymentShowQrActivity;
    }

    public void generateQR() {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(transaction.getId(), BarcodeFormat.QR_CODE, 600, 600);
            requestPaymentShowQrActivity.displayQR(bitmap);
        } catch (WriterException e) {
            requestPaymentShowQrActivity.displayToast("Error Generating QR Code");
            throw new RuntimeException(e);

        }
    }

    public void setTransactionStatusDisplayView(ITransactionStatusDisplayView transactionStatusDisplayView) {
        this.transactionStatusDisplayView = transactionStatusDisplayView;
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

    private static class ValidateInput {

        public static boolean checkValidAmount(String amount) {
            if (isEmpty(amount) && isFloat(amount) && isPositive(Float.parseFloat(amount))) {
                return true;
            } else {
                return false;
            }
        }
        private static boolean isEmpty(String s) {
            return s == null || s.isEmpty();
        }

        private static boolean isPositive(float amount) {
            return amount > 0;
        }

        private static boolean isFloat(String amount) {
            try {
                Float.parseFloat(amount);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}