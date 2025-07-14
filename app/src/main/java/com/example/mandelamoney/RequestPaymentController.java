package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class RequestPaymentController {
    private Context context;
    private IEnterAmount_RequestPaymentView requestPaymentView;
    private IShowQRCode_RequestPaymentView showQRCodeRequestPaymentView;
    private User user;
    private int transactionIdNumeric;
    private Handler handler = new Handler();
    private Runnable statusChecker;
    private boolean isPolling = false;
    public RequestPaymentController(Context context, IEnterAmount_RequestPaymentView requestPaymentView) {
        this.context = context;
        this.requestPaymentView = requestPaymentView;
        this.user = user;

    }

    public RequestPaymentController(Context context, IShowQRCode_RequestPaymentView requestPaymentView) {
        this.context = context;
        this.showQRCodeRequestPaymentView = requestPaymentView;
        this.user = user;
    }


    public void handleGenerateQR(String amount) {
        if (!isValidInput(amount)) {
            //not a valid input
            return;
        }
        float transactionAmount = Float.parseFloat(amount);
        String toUserEmail = UserSession.getUser().getUserEmail().trim();
        Integer transactionID = MySQLConnector.createTransaction(toUserEmail, transactionAmount, context);
        if (transactionID == null) {
            requestPaymentView.showError(context.getString(R.string.could_not_create_transaction));
            return;
        }
        //sending the controller. Use DataShare.receive() to get the controller on next screen. Remember to update the context using setContext method.
        DataShare.send(this);
        Intent intent = new Intent(context, RequestPaymentShowQrActivity.class);
        intent.putExtra("transaction_id", transactionID.toString());
        context.startActivity(intent);
        setContext(context);
    }
    public void startPollingStatus() {
        isPolling = true;

        statusChecker = new Runnable() {
            @Override
            public void run() {
                String status = MySQLConnector.getTransactionStatus(transactionIdNumeric, context);

                if ("success".equalsIgnoreCase(status)) {
                    stopPolling();

                    TransactionDetails txnDetails = MySQLConnector.getTransactionDetailsFromProcedure(
                            transactionIdNumeric, context);

                    if (txnDetails != null) {
                        Intent intent = new Intent(context, ShowSuccessActivity.class);
                        context.startActivity(intent);
                        setContext(context);
                    } else {
                        System.out.println("Transaction completed, but details could not be retrieved.");
                    }

                } else if ("failed".equalsIgnoreCase(status)) {
                    stopPolling();

                    TransactionDetails txnDetails = MySQLConnector.getTransactionDetailsFromProcedure(
                            transactionIdNumeric, context);

                    if (txnDetails != null) {
                        Intent intent = new Intent(context, ShowFailedActivity.class);
                        context.startActivity(intent);
                        setContext(context);
                    } else {
                        System.out.println("Transaction failed, but details could not be retrieved.");
                    }

                } else {
                    // Status still pending
                    handler.postDelayed(this, 3000);
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

        if (ValidateInput.isEmpty(amount)) {
            requestPaymentView.showError(context.getString(R.string.enter_a_valid_amount));
            return false;
        }

        if (!ValidateInput.isDouble(amount)) {
            requestPaymentView.showError(context.getString(R.string.enter_a_valid_amount));
            return false;
        }

        if (!ValidateInput.isPositive(Double.parseDouble(amount))) {
            requestPaymentView.showError(context.getString(R.string.enter_a_positive_amount));
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
            if (s == null) {
                return true;
            }

            if (s.isEmpty()) {
                return true;
            }

            return false;
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
