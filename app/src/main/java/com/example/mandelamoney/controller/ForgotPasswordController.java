package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Patterns;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import com.example.mandelamoney.BuildConfig;
import com.example.mandelamoney.R;
import com.example.mandelamoney.util.EmailSender;
import com.example.mandelamoney.util.Hasher;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.IForgotPasswordView;
import com.example.mandelamoney.view.Iface.IRecoverAccountView;
import com.example.mandelamoney.view.Iface.IResetPasswordView;
import com.example.mandelamoney.view.activity.LoginActivity;
import com.example.mandelamoney.view.activity.RecoverAccountActivity;
import com.example.mandelamoney.view.activity.ResetPasswordActivity;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPasswordController {
    private Context context;

    private final IForgotPasswordView forgotPasswordView;
    private IRecoverAccountView recoverAccountView;
    private IResetPasswordView resetPasswordView;

    private String recoveryCode;
    private String userEmail;

    private final ExecutorService forgotPasswordExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());



    public ForgotPasswordController(Context context, IForgotPasswordView forgotPasswordView){
        this.context = context;
        this.forgotPasswordView = forgotPasswordView;
    }
    public void handleForgotPassword(String userEmail) throws SQLException {
        if(forgotPasswordView != null){
            forgotPasswordView.hideErrorMessage_InvalidEmail();
        }
        final String email = userEmail == null ? "" : userEmail.trim().toLowerCase();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (forgotPasswordView != null) forgotPasswordView.showErrorMessage_InvalidEmail();
            return;
        }

        this.userEmail = email;
        if (forgotPasswordView != null) forgotPasswordView.showLoadingSpinner();

        forgotPasswordExecutor.execute(()->{
            boolean exists = false;
            String hashcode = null;
            try {
                Object[] objs = callSQLForgotPassword(email);
                if (objs != null && objs.length >= 2) {
                    Object ex = objs[0];
                    exists = (ex instanceof Boolean) && (Boolean) ex;
                    hashcode = exists ? (String) objs[1] : null;
                }
            }catch(Exception e){
               exists = false;
               hashcode = null;
            }
            if (exists && hashcode != null && !hashcode.isEmpty()) {
                try {
                    EmailSender sender = new EmailSender(BuildConfig.EMAIL_USERNAME, BuildConfig.EMAIL_PASSWORD);
                    String subject = "Mandela Money Recovery Code";
                    String body = "Your Mandela Money account recovery code is: " + hashcode;
                    sender.sendMail(subject, body, email);
                    android.util.Log.i("EmailSender", "Email sent successfully to " + email);
                } catch (Exception e) {
                    android.util.Log.e("EmailSender", "Failed to send email", e);
                }
            }
            boolean finalExists = exists;

            mainHandler.post(() -> {
                if (forgotPasswordView != null) forgotPasswordView.hideLoadingSpinner();

                Intent intent = new Intent(context, RecoverAccountActivity.class);
                intent.putExtra("userEmail", email);
                intent.putExtra("emailExists", finalExists);
                context.startActivity(intent);
                if (forgotPasswordView != null) forgotPasswordView.finishActivity();
            });
        });

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
         final String code  =  (recoveryCode == null ? "" : recoveryCode.trim().toLowerCase());

        if(recoverAccountView != null){
            recoverAccountView.showLoadingSpinner();
        }

        forgotPasswordExecutor.execute(()->{
            boolean isValid = false;
            try{
                isValid = MySQLConnector.verifyRecoveryCode(userEmail, code, context);
            } catch(Exception e){
                isValid = false;
            }

            final boolean finalIsValid = isValid;
            mainHandler.post(()->{
                if(recoverAccountView != null){
                    recoverAccountView.hideLoadingSpinner();
                }
                if (finalIsValid) {
                    this.recoveryCode = code;
                    Intent intent = new Intent(context, ResetPasswordActivity.class);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("recoveryCode", this.recoveryCode);
                    context.startActivity(intent);
                    if (recoverAccountView != null) recoverAccountView.finishActivity();
                } else {
                    if (recoverAccountView != null) recoverAccountView.showErrorMessage_InvalidCode();
                }
            });
        });

    }


    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


    public void setUserRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    public void handleResetPassword(String newPassword, String confirmNewPassword) {
        if(resetPasswordView == null) return;

        resetPasswordView.hideErrorMessage_PasswordsDoNotMatch();
        resetPasswordView.hideErrorMessage_Minimum8Characters();

        final String password = (newPassword == null ? "" : newPassword.trim());
        final String confirm = (confirmNewPassword == null ? "":confirmNewPassword.trim());

        if (!password.equals(confirm)) {
            resetPasswordView.showErrorMessage_PasswordsDoNotMatch(context.getString(R.string.passwords_do_not_match_error));
            return;
        }

        if (password.length() < 8) {
            resetPasswordView.showErrorMessage_Minimum8Characters(context.getString(R.string.minimum_8_characters));
            return;
        }

        resetPasswordView.showLoadingSpinner();
        forgotPasswordExecutor.execute(()->{
            boolean resetSuccess;
            try {
                resetSuccess = MySQLConnector.resetPassword(userEmail, recoveryCode, Hasher.getHash(password), context);
            }catch(Exception e){
                resetPasswordView.hideLoadingSpinner();
                resetSuccess = false;
            }
            final boolean finalSuccess = resetSuccess;
            mainHandler.post(()->{
                resetPasswordView.hideLoadingSpinner();

                if(finalSuccess) {
                    Toast.makeText(context, "Password reset successful! Please log in.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    resetPasswordView.finishActivity();
                }else{
                    Toast.makeText(context, context.getString(R.string.invalid_recovery_code), Toast.LENGTH_LONG).show();

                }
            });


        });

    }
}
