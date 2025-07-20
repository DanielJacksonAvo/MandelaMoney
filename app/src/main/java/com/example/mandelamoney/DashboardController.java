package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class DashboardController {
    private final IDashboardView view;
    private final Context context;
    private User user;

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
        // TODO: load into UI
    }

    public void handleMakePayment() {
        DataShare.send(this);
        Intent intent = new Intent(context, MakePaymentScanQrActivity.class);
        context.startActivity(intent);
    }

    public void handleRequestPayment() {
        Intent intent = new Intent(context, RequestPaymentEnterAmountActivity.class);
        context.startActivity(intent);
    }

    private void pullSQLTransaction() {
        ArrayList<Transaction> transactionList = new ArrayList<>();
        // TODO: SQL logic to fill transactionList
    }
}
