package com.example.mandelamoney.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class UserSession {

    private static User currentUser;
    private static List<Transaction> cachedTransactionHistory;

    private static final String PREF_NAME = "secure_user_prefs";
    private static final String KEY_USER = "user_data";

    public static User getUser() {
        return currentUser;
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static void deleteSession(Context context) {
        currentUser = null;
        cachedTransactionHistory = null;

        try {
            SharedPreferences prefs = getSecurePrefs(context);
            prefs.edit().remove(KEY_USER).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void clearSession() {
        currentUser = null;
        cachedTransactionHistory = null;
    }


    public static void updateTransactions(Context context) {
        List<Transaction> rawTransactionHistory = MySQLConnector.getTransactionHistory(currentUser.getUserEmail(), context);
        cachedTransactionHistory = TransactionManager.formatTransactionHistory(rawTransactionHistory, context);
    }

    public static List<Transaction> getLastWeekTransactions() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusWeeks(1);
        return cachedTransactionHistory.stream()
                .filter(transaction -> {
                    try {
                        LocalDate transactionDate = LocalDate.parse(transaction.getDate(), formatter);

                        return !transactionDate.isBefore(oneWeekAgo) && !transactionDate.isAfter(today);
                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing date for transaction: " + transaction.getDate() + " - " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
    public static List<Transaction> getLastMonthTransactions() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        LocalDate today = LocalDate.now();
        LocalDate oneMonthAgo = today.minusMonths(1);
        return cachedTransactionHistory.stream()
                .filter(transaction -> {
                    try {
                        LocalDate transactionDate = LocalDate.parse(transaction.getDate(), formatter);
                        return !transactionDate.isBefore(oneMonthAgo) && !transactionDate.isAfter(today);
                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing date for transaction: " + transaction.getDate() + " - " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<Transaction> getLastYearTransactions() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);
        return cachedTransactionHistory.stream()
                .filter(transaction -> {
                    try {
                        LocalDate transactionDate = LocalDate.parse(transaction.getDate(), formatter);

                        return !transactionDate.isBefore(oneYearAgo) && !transactionDate.isAfter(today);
                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing date for transaction: " + transaction.getDate() + " - " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<Transaction> getTransactions() {
        return cachedTransactionHistory;
    }


    public static void saveSession(Context context) {
        if (currentUser == null) return;

        try {
            SharedPreferences prefs = getSecurePrefs(context);
            Gson gson = new Gson();
            String json = gson.toJson(currentUser);
            prefs.edit().putString(KEY_USER, json).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSession(Context context) {
        try {
            SharedPreferences prefs = getSecurePrefs(context);
            String json = prefs.getString(KEY_USER, null);

            if (json != null) {
                Gson gson = new Gson();
                currentUser = gson.fromJson(json, User.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSessionExists(Context context) {
        try {
            SharedPreferences prefs = getSecurePrefs(context);
            return prefs.contains(KEY_USER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static SharedPreferences getSecurePrefs(Context context)
            throws GeneralSecurityException, IOException {

        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        return EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
