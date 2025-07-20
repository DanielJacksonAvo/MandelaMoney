package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class RequestPaymentController {
    private Context context;
    private IEnterAmountRequestPaymentView requestPaymentView;
    private IShowQRCodeRequestPaymentView showQRCodeRequestPaymentView;
    private int transactionIdNumeric;
    private Handler handler = new Handler();
    private Runnable statusChecker;
    private boolean isPolling = false;

    public RequestPaymentController(Context context, IEnterAmountRequestPaymentView requestPaymentView) {
        this.context = context;
        this.requestPaymentView = requestPaymentView;
    }

    public RequestPaymentController(Context context, IShowQRCodeRequestPaymentView requestPaymentView) {
        this.context = context;
        this.showQRCodeRequestPaymentView = requestPaymentView;
    }

    public void handleGenerateQR(String amount) {
        if (!isValidInput(amount)) return;

        float transactionAmount = Float.parseFloat(amount);
        String toUserEmail = UserSession.getUser().getUserEmail().trim();
        Integer transactionID = MySQLConnector.createTransaction(toUserEmail, transactionAmount, context);

        if (transactionID == null) {
            requestPaymentView.showError(context.getString(R.string.could_not_create_transaction));
            return;
        }

        DataShare.send(this);
        Intent intent = new Intent(context, RequestPaymentShowQrActivity.class);
        intent.putExtra("transaction_id", transactionID.toString());
        context.startActivity(intent);
        setContext(context);
    }

    public void startPollingStatus(int txnId) {
        this.transactionIdNumeric = txnId;
        isPolling = true;

        statusChecker = new Runnable() {
            @Override
            public void run() {
                String status = MySQLConnector.getTransactionStatus(transactionIdNumeric, context);

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
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.post(statusChecker);
    }

    private void stopPolling() {
        if (isPolling && handler != null && statusChecker != null) {
            handler.removeCallbacks(statusChecker);
            isPolling = false;
        }
    }

    private boolean isValidInput(String amount) {
        requestPaymentView.hideError();
        if (ValidateInput.isEmpty(amount) || !ValidateInput.isDouble(amount) || !ValidateInput.isPositive(Double.parseDouble(amount))) {
            requestPaymentView.showError(context.getString(R.string.enter_a_valid_amount));
            return false;
        }
        return true;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void handleCancelButton() {
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
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
