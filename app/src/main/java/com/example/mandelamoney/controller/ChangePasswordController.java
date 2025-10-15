package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.Hasher;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IChangePasswordView;
import com.example.mandelamoney.R;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.activity.LoginActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChangePasswordController {
    private static final String TAG = "ChangePassword";
    private final Context context;
    private final IChangePasswordView changePasswordView;
    private final ExecutorService changePasswordExecutor = Executors.newSingleThreadExecutor();


    public ChangePasswordController(Context context, IChangePasswordView changePasswordView) {
        this.context = context;
        this.changePasswordView = changePasswordView;
    }
    public void handleChangePassword(String userEmail, String oldPassword, String newPassword, String confirmNewPassword){
        Log.d(TAG, "handleChangePassword() invoked");

        if (changePasswordView != null) {
            changePasswordView.hideCurrentPasswordError();
            changePasswordView.hideNewPasswordError();
        }
        userEmail = userEmail == null ? "" : userEmail.trim();
        oldPassword = oldPassword == null ? "" : oldPassword.trim();
        newPassword = newPassword == null ? "" : newPassword.trim();
        confirmNewPassword = confirmNewPassword == null ? "" : confirmNewPassword.trim();

        boolean hasError = false;
        boolean curPasswordError = false;

        if (oldPassword.isEmpty() || oldPassword.length() < 8) {
            if (changePasswordView != null) {
                changePasswordView.showCurrentPasswordError(context.getString(R.string.incorrect_password_error));
            }
            hasError = true;
            curPasswordError = true;
        }

        if (newPassword.length() < 8) {
            if (changePasswordView != null) {
                changePasswordView.showNewPasswordError(context.getString(R.string.minimum_8_characters));
            }
            hasError = true;
        } else if (!newPassword.equals(confirmNewPassword)) {
            if (changePasswordView != null) {
                changePasswordView.showNewPasswordError(context.getString(R.string.passwords_do_not_match_error));
            }
            hasError = true;
        }
        if (hasError && curPasswordError) return;

        if (changePasswordView != null) changePasswordView.showLoadingSpinner();

        final String finalUserEmail = userEmail;
        final String finalOldPassword = oldPassword;
        final String finalNewPassword = newPassword;
        final boolean finalHasError = hasError;

        changePasswordExecutor.execute(() -> {
            try {
                boolean isMatch = MySQLConnector.verifyPassword(
                        finalUserEmail,
                        Hasher.getHash(finalOldPassword),
                        context
                );

                if (!isMatch) {
                    ContextCompat.getMainExecutor(context).execute(() -> {
                        if (changePasswordView != null) {
                            changePasswordView.hideLoadingSpinner();
                            changePasswordView.showCurrentPasswordError(
                                    context.getString(R.string.incorrect_password_error)
                            );
                        }
                    });
                    return;
                }

                if (finalHasError) {
                    ContextCompat.getMainExecutor(context).execute(() -> {
                        if (changePasswordView != null) {
                            changePasswordView.hideLoadingSpinner();
                        }
                    });
                    return;
                }

                boolean changed = MySQLConnector.changePassword(
                        finalUserEmail,
                        Hasher.getHash(finalOldPassword),
                        Hasher.getHash(finalNewPassword),
                        context
                );

                ContextCompat.getMainExecutor(context).execute(() -> {
                    if (changePasswordView != null) changePasswordView.hideLoadingSpinner();

                    if (changed) {
                        if (UserSession.getUser() != null) {
                            UserSession.getUser().setUserPassword(finalNewPassword);
                            UserSession.saveSession(context);
                        }
                        Toast.makeText(context, "Password changed successfully! Please log in.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        if (changePasswordView != null) changePasswordView.finishActivity();
                    } else {
                        Toast.makeText(context, context.getString(R.string.invalid_recovery_code), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Throwable t) {
                Log.e(TAG, "Unexpected error during changePassword flow", t);
                ContextCompat.getMainExecutor(context).execute(() -> {
                    if (changePasswordView != null) changePasswordView.hideLoadingSpinner();
                    Toast.makeText(context, "Unexpected error. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private static String safe(String s) { return s == null ? "(null)" : s.trim(); }



    public void handleCancel(){
        Log.d(TAG, "handleCancelDepositFunds()");
        DataShare.send(this);
        if (changePasswordView != null) {
            changePasswordView.finishActivity();
            Log.d(TAG, "finishActivity() from handleCancelDepositFunds");
        }
    }

}
