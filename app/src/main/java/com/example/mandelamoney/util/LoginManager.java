package com.example.mandelamoney.util;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.view.activity.DashboardActivity;

public class LoginManager {

    /**
     * Attempts to log in the user in a background thread.
     *
     * @param context      application or activity context
     * @param email        user email
     * @param password     user password
     * @param onSuccess    code to run if login is successful (this can start the DashboardActivity)
     * @param onFailure    code to run if login fails
     */
    public static void login(
            @NonNull Context context,
            @NonNull String email,
            @NonNull String password,
            @NonNull Runnable onSuccess,
            @NonNull Runnable onFailure
    ) {
        new Thread(() -> {
            Object[] result = MySQLConnector.validateEmailPassword(email, password, context);
            if (result == null || !(boolean) result[1] || !(result[0] instanceof User)) {
                runOnMainThread(context, onFailure);
                return;
            }
            User user = (User) result[0];
            UserSession.setUser(user);
            UserSession.updateTransactions(context);
            UserSession.saveSession(context);
            runOnMainThread(context, onSuccess);
        }).start();
    }

    private static void runOnMainThread(Context context, Runnable runnable) {
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        handler.post(runnable);
    }
}
