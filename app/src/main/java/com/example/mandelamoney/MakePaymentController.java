package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

public class MakePaymentController {
    private IConfirmPaymentView confirmPaymentView;
    private Context context;
    public MakePaymentController(Context context) {
        this.context = context;
    }

    public void setConfirmView(IConfirmPaymentView view) {
        confirmPaymentView = view;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void handleConfirmPayment() {
        /// confirms, and moves to next screen.
        DataShare.send(this);
    }

    public void handleCancel() {
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
    }
}
