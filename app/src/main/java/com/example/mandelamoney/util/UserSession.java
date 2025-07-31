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
import java.util.List;

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
        cachedTransactionHistory = MySQLConnector.getTransactionHistory(currentUser.getUserEmail(), context);
    }


    public static List<Transaction> getCachedTransactionHistory() {
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
