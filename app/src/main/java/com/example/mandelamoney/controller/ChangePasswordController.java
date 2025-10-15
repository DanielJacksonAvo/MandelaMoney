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
        final String TAG = "ChangePassword";
        Log.d(TAG, "handleChangePassword() invoked");

        if(changePasswordView != null){
            changePasswordView.hideCurrentPasswordError();
            changePasswordView.hideNewPasswordError();
        }
        boolean hasError = false;

        boolean isMatch = MySQLConnector.verifyPassword(userEmail, Hasher.getHash(oldPassword), context);
        if (!isMatch) {
            Log.w(TAG, "Password verification failed");
            if(changePasswordView != null) changePasswordView.showCurrentPasswordError(context.getString(R.string.incorrect_password_error));
            hasError = true;
        }

        if (newPassword.length() < 8) {
            Log.w(TAG, "New password length < 8");
           if(changePasswordView != null) changePasswordView.showNewPasswordError(context.getString(R.string.minimum_8_characters));
           hasError = true;
        }else if (!newPassword.equals(confirmNewPassword)) {
            Log.d(TAG, "New password passes length check");
            Log.w(TAG, "New password and confirmation do not match");
            if(changePasswordView != null) changePasswordView.showNewPasswordError(context.getString(R.string.passwords_do_not_match_error));
            hasError=true;
        }
        if(hasError) return;
        changePasswordExecutor.execute(()->{
            try{
                User current = UserSession.getUser();
                Log.d(TAG, "BG: fetched current user = " + (current != null ? safe(current.getUserEmail()) : "null"));

                if(current == null){
                    ContextCompat.getMainExecutor(context).execute(() -> {
                        Log.w(TAG, "BG->UI: session expired, notifying view");
                        Toast.makeText(context.getApplicationContext(),
                                context.getString(R.string.session_expired),
                                Toast.LENGTH_LONG).show();
                    });
                    return;

                }
                boolean changePasswordSuccess = MySQLConnector.changePassword(userEmail, Hasher.getHash(oldPassword),Hasher.getHash(newPassword), context);

                if (changePasswordSuccess) {
                    UserSession.getUser().setUserPassword(newPassword);
                    UserSession.saveSession(context);

                    Log.i(TAG, "Password change succeeded; redirecting to LoginActivity");
                    Toast.makeText(context, "Password changed successful! Please log in.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    if(changePasswordView!= null) changePasswordView.finishActivity();
                } else {
                    Log.e(TAG, "Password change failed (connector returned false)");
                    Toast.makeText(context, context.getString(R.string.invalid_recovery_code), Toast.LENGTH_LONG).show();
                }
            }catch (Throwable t){
                Log.e(TAG, "BG: Unexpected error during withdraw flow", t);
                ContextCompat.getMainExecutor(context).execute(() -> {
                    if(changePasswordView!=null) changePasswordView.hideLoadingSpinner();
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
