package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

public class DashboardController {
    private final IDashboardView view;
    private final Context context;
    private User user;

    public DashboardController(Context context, IDashboardView view, User user) {
        this.context = context;
        this.view = view;
        this.user = user;
    }

    public void handleLoadUserToUI() {
        //puts user data to ui
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

    }

    public void handleReceivePayment() {

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
