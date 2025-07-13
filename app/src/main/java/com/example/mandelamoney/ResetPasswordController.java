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
        this.userEmail = userEmail;
    }

    public void setUserRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    public void handleResetPassword(String newPassword, String confirmNewPassword) {

        view.hideErrorMessage_PasswordsDoNotMatch();
        view.hideErrorMessage_Minimum8Characters();
        if (!newPassword.equals(confirmNewPassword)) {
            view.showErrorMessage_PasswordsDoNotMatch(context.getString(R.string.passwords_do_not_match_error));
            return;
        }

        if (newPassword.length() < 8) {
            view.showErrorMessage_Minimum8Characters(context.getString(R.string.minimum_8_characters));
            return;
        }
        boolean resetSuccess = MySQLConnector.resetPassword(userEmail, recoveryCode, newPassword, context);

        if (resetSuccess) {
            Toast.makeText(context, "Password reset successful! Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            view.finishActivity();
        } else {
            Toast.makeText(context, context.getString(R.string.invalid_recovery_code), Toast.LENGTH_LONG).show();
        }
    }

    public void handleCancel() {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }
}
