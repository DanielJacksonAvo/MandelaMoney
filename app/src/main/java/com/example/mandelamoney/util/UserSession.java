package com.example.mandelamoney.util;

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

}
