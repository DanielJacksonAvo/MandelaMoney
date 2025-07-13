package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

public class RequestPaymentController {
    private Context context;
    private IEnterAmount_RequestPaymentView requestPaymentView;
    public RequestPaymentController(Context context, IEnterAmount_RequestPaymentView requestPaymentView) {
        this.context = context;
        this.requestPaymentView = requestPaymentView;

    }


    public void handleGenerateQR(String amount) {
        if (!isValidInput(amount)) {
            //not a valid input
            return;
        }

        //sending the controller. Use DataShare.receive() to get the controller on next screen. Remember to update the context using setContext method.
        DataShare.send(this);

        /// move to next screen & generate qr ///
        Intent intent = new Intent(context, RequestPayment_ShowQRActivity.class);
        intent.putExtra("transaction_id", "1000"); //Needs to get altered to the actual QR Code
        context.startActivity(intent);
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
