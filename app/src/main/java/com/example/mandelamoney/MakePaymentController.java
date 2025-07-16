package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

public class MakePaymentController {
    private IConfirmPaymentView confirmPaymentView;
    private Context context;
    private int transactionId;

    public MakePaymentController(Context context) {
        this.context = context;
    }

    public void setConfirmView(IConfirmPaymentView view) {
        confirmPaymentView = view;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setTransactionId(int id) {
        this.transactionId = id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void handleConfirmPayment() {
        // Send this controller to the next screen via DataShare
        DataShare.send(this);
    }

    public void handleCancel() {
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
    }
}
