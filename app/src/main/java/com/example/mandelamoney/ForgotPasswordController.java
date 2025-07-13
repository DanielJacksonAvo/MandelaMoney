package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

import java.sql.SQLException;
public class ForgotPasswordController {
    private final Context context;

    private IForgotPasswordView view;

    private User user;
    public ForgotPasswordController(Context context, IForgotPasswordView view){
        this.context = context;
        this.view = view;
    }
    public void handleForgotPassword(String userEmail) throws SQLException {
        if (userEmail.length() < 5) {
            view.showErrorMessage_InvalidEmail();
            return;
        }

        Object[] objs = callSQLForgotPassword(userEmail);
        if (objs == null || !(boolean) objs[0]) {
            //view.showErrorMessage_InvalidEmail();
            return;
        }

        String hashcode = (String) objs[1];
        if (hashcode == null || hashcode.isEmpty()) {
            //view.showErrorMessage_InvalidEmail();
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


        view.hideErrorMessage_InvalidEmail();
        Intent intent = new Intent(context, RecoverAccountActivity.class);
        intent.putExtra("userEmail", userEmail);
        context.startActivity(intent);
        view.finishActivity();
    }

    public void handleCancel(){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }
    private Object[] callSQLForgotPassword(String userEmail){
       return MySQLConnector.getRecoveryCodeHash(userEmail,context);
    }

}
