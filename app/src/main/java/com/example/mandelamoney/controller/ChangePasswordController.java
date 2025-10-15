package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IChangePasswordView;
import com.example.mandelamoney.R;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.activity.LoginActivity;


public class ChangePasswordController {
    private static final String TAG = "ChangePassword";
    private Context context;
    private final IChangePasswordView changePasswordView;

    public ChangePasswordController(Context context, IChangePasswordView changePasswordView) {
        this.context = context;
        this.changePasswordView = changePasswordView;
    }
    public void handleChangePassword(String userEmail, String oldPassword, String newPassword, String confirmNewPassword){
        final String TAG = "ChangePassword";
        Log.d(TAG, "handleChangePassword() invoked");

        changePasswordView.hideErrorMessage();
        Log.d(TAG, "Verifying current password via MySQLConnector.verifyPassword()");
        boolean isMatch = MySQLConnector.verifyPassword(userEmail, oldPassword, context);

        if (!isMatch) {
            Log.w(TAG, "Password verification failed");
            changePasswordView.showErrorMessage(context.getString(R.string.incorrect_password_error));
            return;
        }
        Log.d(TAG, "Password verification succeeded");

        if (!newPassword.equals(confirmNewPassword)) {
            Log.w(TAG, "New password and confirmation do not match");
            changePasswordView.showErrorMessage(context.getString(R.string.passwords_do_not_match_error));
            return;
        }
        Log.d(TAG, "New password and confirmation match");

        if (newPassword.length() < 8) {
            Log.w(TAG, "New password length < 8");
            changePasswordView.showErrorMessage(context.getString(R.string.minimum_8_characters));
            return;
        }
        Log.d(TAG, "New password passes length check");

        Log.d(TAG, "Calling MySQLConnector.changePassword()");
        boolean changePasswordSuccess = MySQLConnector.changePassword(userEmail, oldPassword, newPassword, context);

        if (changePasswordSuccess) {
            Log.i(TAG, "Password change succeeded; redirecting to LoginActivity");
            Toast.makeText(context, "Password changed successful! Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            changePasswordView.finishActivity();
        } else {
            Log.e(TAG, "Password change failed (connector returned false)");
            Toast.makeText(context, context.getString(R.string.invalid_recovery_code), Toast.LENGTH_LONG).show();
        }
    }

    public void handleCancel(){
        Log.d(TAG, "handleCancelDepositFunds()");
        DataShare.send(this);
        if (changePasswordView != null) {
            changePasswordView.finishActivity();
            Log.d(TAG, "finishActivity() from handleCancelDepositFunds");
        }
    }

}
