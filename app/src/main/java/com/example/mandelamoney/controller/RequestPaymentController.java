package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Handler;
import android.util.Log; // Import Log for debugging

import androidx.core.content.ContextCompat; // For getting main executor

import com.example.mandelamoney.R;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEnterAmountRequestPaymentView;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;
import com.example.mandelamoney.view.Iface.IShowQRCodeRequestPaymentView; // Not directly used but good to keep
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.RequestPaymentShowQrActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;
import com.example.mandelamoney.view.activity.ShowSuccessActivity;

import java.util.concurrent.ExecutorService; // New import
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RequestPaymentController {
    private Context context;
    private IEnterAmountRequestPaymentView requestPaymentView;
    private IHomeDashboardView homeDashboardView;
    private int transactionIdNumeric;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private ScheduledFuture<?> pollingHandle;
    private Runnable statusChecker;

    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(); // New executor for single requests

    public RequestPaymentController(Context context, Object obj) {
        this.context = context;
        if (obj instanceof IEnterAmountRequestPaymentView){
            requestPaymentView = (IEnterAmountRequestPaymentView) obj;
        }

        if (obj instanceof IHomeDashboardView) {
            homeDashboardView = (IHomeDashboardView) obj;
        }
    }


    public void handleGenerateQR(String amount) {
        if (!isValidInput(amount)) {
            Log.d("RequestPaymentController", "Invalid input for QR generation.");
            return;
        }

        // Execute the QR generation logic on a separate thread
        requestExecutor.execute(() -> {
            float transactionAmount = Float.parseFloat(amount);
            String toUserEmail = UserSession.getUser().getUserEmail().trim();

            Log.d("RequestPaymentController", "Attempting to create transaction for amount: " + transactionAmount);
            Integer transactionID = MySQLConnector.createTransaction(toUserEmail, transactionAmount, context);

            // Switch back to the main thread to update UI
            ContextCompat.getMainExecutor(context).execute(() -> {
                if (transactionID == null) {
                    Log.e("RequestPaymentController", "Failed to create transaction: transactionID is null.");
                    if (requestPaymentView != null) {
                        requestPaymentView.showError(context.getString(R.string.could_not_create_transaction));
                    }
                    return;
                }

                Log.d("RequestPaymentController", "Transaction created with ID: " + transactionID);
                DataShare.send(this);
                Intent intent = new Intent(context, RequestPaymentShowQrActivity.class);
                intent.putExtra("transaction_id", transactionID.toString());
                context.startActivity(intent);
            });
        });
    }

    public void startPollingStatus(int txnId) {
        if (pollingHandle != null && !pollingHandle.isDone()) {
            Log.d("RequestPaymentController", "Polling already active or pending.");
            return;
        }
        this.transactionIdNumeric = txnId;
        Log.d("RequestPaymentController", "Starting polling for transaction ID: " + txnId);


        statusChecker = () -> {
            try {
                String status = MySQLConnector.getTransactionStatus(transactionIdNumeric, context);
                Log.d("RequestPaymentController", "Polling status for " + transactionIdNumeric + ": " + status);
                mainThreadHandler.post(() -> {
                    if ("success".equalsIgnoreCase(status)) {
                        stopPolling();
                        Log.d("RequestPaymentController", "Transaction " + transactionIdNumeric + " succeeded. Navigating to success screen.");
                        Intent intent = new Intent(context, ShowSuccessActivity.class);
                        intent.putExtra("TRANSACTION_ID", transactionIdNumeric);
                        DataShare.send(RequestPaymentController.this);
                        context.startActivity(intent);
                    } else if ("failed".equalsIgnoreCase(status)) {
                        stopPolling();
                        Log.d("RequestPaymentController", "Transaction " + transactionIdNumeric + " failed. Navigating to failed screen.");
                        Intent intent = new Intent(context, ShowFailedActivity.class);
                        intent.putExtra("TRANSACTION_ID", transactionIdNumeric);
                        intent.putExtra("ERROR_REASON", "Transaction was not completed.");
                        DataShare.send(RequestPaymentController.this);
                        context.startActivity(intent);
                    }
                });
            } catch (Exception e) {
                Log.e("RequestPaymentController", "Error during polling status: " + e.getMessage(), e);
                // Optionally, stop polling or show an error toast on the main thread if severe
                mainThreadHandler.post(() -> {
                    // You might want to stop polling on a persistent error to avoid spamming logs
                    // stopPolling();
                    // Toast.makeText(context, "Error checking transaction status.", Toast.LENGTH_SHORT).show();
                });
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

    private boolean isValidInput(String amount) {
        // All Toast/UI updates for isValidInput should ideally be on the main thread
        // This method is called from handleGenerateQR, which itself is now on a background thread.
        // So, any direct UI calls from here need to be posted to the main thread.
        if (requestPaymentView != null) {
            requestPaymentView.hideError();
            if (ValidateInput.isEmpty(amount) || !ValidateInput.isDouble(amount) || !ValidateInput.isPositive(Double.parseDouble(amount))) {
                ContextCompat.getMainExecutor(context).execute(() ->
                        requestPaymentView.showError(context.getString(R.string.enter_a_valid_amount))
                );
                return false;
            }
        } else {
            if (homeDashboardView != null) {
                if (ValidateInput.isEmpty(amount) || !ValidateInput.isDouble(amount) || !ValidateInput.isPositive(Double.parseDouble(amount))) {
                    return false; // No UI to update, just return false
                }
            } else { return false; } // No view to update, just return false
        }
        return true;
    }

    public void setContext(Context context) {
        this.context = context;
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

    private static class ValidateInput {
        public static boolean isEmpty(String s) {
            return s == null || s.isEmpty();
        }

        public static boolean isPositive(double amount) {
            return amount > 0;
        }

        public static boolean isDouble(String amount) {
            try {
                Double.parseDouble(amount);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}