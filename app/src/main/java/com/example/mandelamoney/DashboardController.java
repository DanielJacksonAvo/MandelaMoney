package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class DashboardController {
    private final IDashboardView view;
    private final Context context;
    private User user;
    private Handler handler = new Handler();

    private boolean isPolling;
    private Runnable statusChecker;


    public DashboardController(Context context, IDashboardView view) {
        this.context = context;
        this.view = view;
        this.user = UserSession.getUser();
    }

    public void handleLoadUserToUI() {
        view.displayBalance(user.getUserBalance());
        if (user instanceof Student) {
            String fullname = ((Student) user).getStudentFirstName() + " " + ((Student) user).getStudentLastName();
            view.displayUserName(fullname);
        } else if (user instanceof Business) {
            view.displayUserName(((Business) user).getBusinessName());
        }

        startPolling();
    }

    public void handleBalanceRefresh() {
        if (user != null) {
            double updatedBalance = MySQLConnector.getUserBalance(user.getUserEmail(), context);
            user.setUserBalance(updatedBalance); // Update in-session user object
            view.displayBalance(updatedBalance);
        }
    }

    public void handleLoadTransactionsToUI() {
        pullSQLTransaction();
    }

    public void handleMakePayment() {
        DataShare.send(this);
        stopPolling();
        Intent intent = new Intent(context, MakePaymentScanQrActivity.class);
        context.startActivity(intent);
    }

    public void handleRequestPayment() {
        stopPolling();
        Intent intent = new Intent(context, RequestPaymentEnterAmountActivity.class);
        context.startActivity(intent);
    }

    private void pullSQLTransaction() {
        ArrayList<Transaction> transactionList = new ArrayList<>();
        // TODO: SQL logic to fill transactionList
    }

    public void startPolling() {
        isPolling = true;

        statusChecker = new Runnable() {
            @Override
            public void run() {
                handleBalanceRefresh();
                handler.postDelayed(this, 5000);
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

}
