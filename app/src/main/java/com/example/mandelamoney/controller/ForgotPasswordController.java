package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.mandelamoney.BuildConfig;
import com.example.mandelamoney.R;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.EmailSender;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.IForgotPasswordView;
import com.example.mandelamoney.view.Iface.IRecoverAccountView;
import com.example.mandelamoney.view.Iface.IResetPasswordView;
import com.example.mandelamoney.view.activity.LoginActivity;
import com.example.mandelamoney.view.activity.RecoverAccountActivity;
import com.example.mandelamoney.view.activity.ResetPasswordActivity;

import java.sql.SQLException;
public class ForgotPasswordController {
    private Context context;

    private final IForgotPasswordView forgotPasswordView;
    private IRecoverAccountView recoverAccountView;
    
    private IResetPasswordView resetPasswordView;

    private String recoveryCode;
    private String userEmail;


    public ForgotPasswordController(Context context, IForgotPasswordView forgotPasswordView){
        this.context = context;
        this.forgotPasswordView = forgotPasswordView;
    }
    public void handleForgotPassword(String userEmail) throws SQLException {
        if (userEmail.length() < 5) {
            forgotPasswordView.showErrorMessage_InvalidEmail();
            return;
        }

        Object[] objs = callSQLForgotPassword(userEmail);
        if (!(boolean) objs[0]) {
            //forgotPasswordView.showErrorMessage_InvalidEmail();
            return;
        }

        String hashcode = (String) objs[1];
        if (hashcode == null || hashcode.isEmpty()) {
            //forgotPasswordView.showErrorMessage_InvalidEmail();
            return;
        }

        new Thread(() -> {
            try {
                EmailSender sender = new EmailSender(BuildConfig.EMAIL_USERNAME, BuildConfig.EMAIL_PASSWORD);
                String subject = "Mandela Money Recovery Code";
                String body = "Your Mandela Money account recovery code is: " + hashcode;
                sender.sendMail(subject, body, userEmail);
                android.util.Log.i("EmailSender", "Email sent successfully to " + userEmail);
            } catch (Exception e) {
                android.util.Log.e("EmailSender", "Failed to send email", e);
            }
        }).start();


        forgotPasswordView.hideErrorMessage_InvalidEmail();
        Intent intent = new Intent(context, RecoverAccountActivity.class);
        intent.putExtra("userEmail", userEmail);
        DataShare.send(this);
        context.startActivity(intent);
        forgotPasswordView.finishActivity();
    }

    public void handleCancel(){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        forgotPasswordView.finishActivity();
    }
    private Object[] callSQLForgotPassword(String userEmail){
        return MySQLConnector.getRecoveryCodeHash(userEmail,context);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setRecoverAccountView(IRecoverAccountView view) {
        recoverAccountView = view;
    }
    
    public void setResetPasswordView(IResetPasswordView view) {
        resetPasswordView = view;
    }

    public void handleVerify(String recoveryCode) throws SQLException {
        this.recoveryCode = recoveryCode.toLowerCase();
        boolean isValid = MySQLConnector.verifyRecoveryCode(userEmail, recoveryCode, context);
        if (isValid) {
            DataShare.send(this);
            Intent intent = new Intent(context, ResetPasswordActivity.class);
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("recoveryCode",this.recoveryCode);
            context.startActivity(intent);
            recoverAccountView.finishActivity();
        } else {
            recoverAccountView.showErrorMessage_InvalidCode();
        }
    }


    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


    public void setUserRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    public void handleResetPassword(String newPassword, String confirmNewPassword) {

        resetPasswordView.hideErrorMessage_PasswordsDoNotMatch();
        resetPasswordView.hideErrorMessage_Minimum8Characters();
        if (!newPassword.equals(confirmNewPassword)) {
            resetPasswordView.showErrorMessage_PasswordsDoNotMatch(context.getString(R.string.passwords_do_not_match_error));
            return;
        }

        if (newPassword.length() < 8) {
            resetPasswordView.showErrorMessage_Minimum8Characters(context.getString(R.string.minimum_8_characters));
            return;
        }
        boolean resetSuccess = MySQLConnector.resetPassword(userEmail, recoveryCode, newPassword, context);

        if (resetSuccess) {
            Toast.makeText(context, "Password reset successful! Please log in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            resetPasswordView.finishActivity();
        } else {
            Toast.makeText(context, context.getString(R.string.invalid_recovery_code), Toast.LENGTH_LONG).show();
        }
    }
}
