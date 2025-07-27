package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Handler;

import com.example.mandelamoney.R;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEnterAmountRequestPaymentView;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;
import com.example.mandelamoney.view.Iface.IShowQRCodeRequestPaymentView;
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.RequestPaymentShowQrActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;
import com.example.mandelamoney.view.activity.ShowSuccessActivity;

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
        if (!isValidInput(amount)) return;

        float transactionAmount = Float.parseFloat(amount);
        String toUserEmail = UserSession.getUser().getUserEmail().trim();
        Integer transactionID = MySQLConnector.createTransaction(toUserEmail, transactionAmount, context);

        if (transactionID == null) {
            if (requestPaymentView != null) {
                requestPaymentView.showError(context.getString(R.string.could_not_create_transaction));
            }
            return;
        }

        DataShare.send(this);
        Intent intent = new Intent(context, RequestPaymentShowQrActivity.class);
        intent.putExtra("transaction_id", transactionID.toString());
        context.startActivity(intent);
    }

    public void startPollingStatus(int txnId) {
        if (pollingHandle != null && !pollingHandle.isDone()) {
            return;
        }
        this.transactionIdNumeric = txnId;

        statusChecker = () -> {
            try {
                String status = MySQLConnector.getTransactionStatus(transactionIdNumeric, context);
                mainThreadHandler.post(() -> {
                    if ("success".equalsIgnoreCase(status)) {
                        stopPolling();
                        Intent intent = new Intent(context, ShowSuccessActivity.class);
                        intent.putExtra("TRANSACTION_ID", transactionIdNumeric);
                        DataShare.send(RequestPaymentController.this);
                        context.startActivity(intent);
                    } else if ("failed".equalsIgnoreCase(status)) {
                        stopPolling();
                        Intent intent = new Intent(context, ShowFailedActivity.class);
                        intent.putExtra("TRANSACTION_ID", transactionIdNumeric);
                        intent.putExtra("ERROR_REASON", "Transaction was not completed.");
                        DataShare.send(RequestPaymentController.this);
                        context.startActivity(intent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        pollingHandle = scheduler.scheduleAtFixedRate(statusChecker, 0, 1, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        if (pollingHandle != null) {
            pollingHandle.cancel(false);
            pollingHandle = null;
        }
    }

    private boolean isValidInput(String amount) {
        if (requestPaymentView != null) {
            requestPaymentView.hideError();
            if (ValidateInput.isEmpty(amount) || !ValidateInput.isDouble(amount) || !ValidateInput.isPositive(Double.parseDouble(amount))) {
                requestPaymentView.showError(context.getString(R.string.enter_a_valid_amount));
                return false;
            }
        } else {
            if (homeDashboardView != null) {
                if (ValidateInput.isEmpty(amount) || !ValidateInput.isDouble(amount) || !ValidateInput.isPositive(Double.parseDouble(amount))) {
                    return false;
                }
            } else { return false; }
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
        mainThreadHandler.removeCallbacksAndMessages(null);
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
