package com.example.mandelamoney.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mandelamoney.model.TransactionDetails;
import com.example.mandelamoney.model.User;

import java.util.List;

public class UserSession {
    private static User currentUser;
    private static List<TransactionDetails> cachedTransactionHistory;

    public static User getUser() {
        return currentUser;
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
        cachedTransactionHistory = null;
    }

    public static void setCachedTransactionHistory(List<TransactionDetails> transactions) {
        cachedTransactionHistory = transactions;
    }

    public static List<TransactionDetails> getCachedTransactionHistory() {
        return cachedTransactionHistory;
    }

    public static void refreshTransactionHistory(Context context, Runnable callback) {
        Log.d("UserSession", "Refreshing transaction history...");

        new Thread(() -> {
            String email = getUser().getUserEmail();
            Log.d("UserSession", "Fetching history for: " + email);

            List<TransactionDetails> newTransactions = MySQLConnector.getTransactionHistory(email, context);
            Log.d("UserSession", "Transactions fetched: " + newTransactions.size());

            setCachedTransactionHistory(newTransactions);

            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(callback);
            }
        }).start();
    }

}
