package com.example.mandelamoney;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ResetPasswordController {
    private final Context context;
    private final IResetPasswordView view;
    private String recoveryCode;
    private String userEmail;
    public ResetPasswordController(Context context, IResetPasswordView view){
        this.context = context;
        this.view = view;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail =userEmail;
    }
    public void handleResetPassword(String newPassword, String confirmNewPassword) {
        // 1) Check if passwords match
        if (!newPassword.equals(confirmNewPassword)) {
            // Show error: passwords do not match
        }
        // 1.1) Check password length >= 8
        if (newPassword.length() < 8) {
            //Show Error
            return;
        }
        boolean resetSuccess = MySQLConnector.resetPassword(userEmail, recoveryCode, newPassword, context);


        if (resetSuccess) {
            Toast.makeText(context, "Password reset successful! Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            view.finishActivity();
        } else {
            Toast.makeText(context, "Password reset failed. Please check your recovery code and try again.", Toast.LENGTH_LONG).show();
        }
    }

    public void handleCancel(){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }

    public void setUserRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }
}
