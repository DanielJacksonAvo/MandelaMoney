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
        if (user != null) {
            double balance = user.getUserBalance();
            // load into UI
        } else {
            // Fallback or redirect to login
            Log.e("DashboardController", "User is null. Redirecting to login.");
            // Optionally: redirect to login screen
        }

    }

    public void handleLoadUserToUI() {
        view.displayBalance(user.getUserBalance());
        if (user instanceof Student) {
            String fullname = ((Student)user).getStudentFirstName() + " " + ((Student)user).getStudentLastName();
            view.displayUserName(fullname);
        }
        if (user instanceof Business) {
            view.displayUserName((((Business) user).getBusinessName()));
        }
    }

    public void handleBalanceRefresh() {
        pullSQLUserBalance();
        view.displayBalance(user.getUserBalance());
    }

    public void handleLoadTransactionsToUI() {
        pullSQLTransaction();
        //load arraylist to ui

    }

    public void handleMakePayment() {
        DataShare.send(this);  // Persist controller to next screen
        Intent intent = new Intent(context, MakePaymentScanQrActivity.class);
        context.startActivity(intent);
    }


    public void handleRequestPayment() {
        Intent intent = new Intent(context, RequestPaymentEnterAmountActivity.class);
        context.startActivity(intent);
    }

    public void handleWithdraw() {

    }

    public void handleViewAllTransactions() {

    }

    //Pulls balance from SQL server and updates the user object
    private void pullSQLUserBalance() {
        //double balance = call SQL procedure to get balance
        //user.setUserBalance(balance);
    }

    private void pullSQLTransaction(){
        ArrayList<Transaction> transactionList = new ArrayList<>();
        //sql must get recent transaction and fill into array
    }




}
