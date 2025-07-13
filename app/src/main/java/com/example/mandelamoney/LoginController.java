package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

import java.sql.SQLException;
import android.content.SharedPreferences;

public class LoginController {
    private final Context context;
    private final ILoginView view;
    private User user;
    public LoginController(Context context, ILoginView view) {
        this.context = context;
        this.view = view;

    }

    public void handleLogin(String userEmail, String userPassword) throws SQLException {
        if ((userEmail.length() < 5) || (userPassword.length() < 5))
        {
            view.showErrorMessage();
            return;
        }

        Object[] objs = callSQLLogin(userEmail, userPassword);

        if(objs == null || !((boolean) objs[1])) {
            return;
        }

        user = (User)objs[0];

        if (user == null) {
            view.showErrorMessage();
            return;
        }

        view.hideErrorMessage();
        UserSession.setUser(user);
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userEmail", user.getUserEmail());
        if (user instanceof Student) {
            editor.putString("userType", "student");
        } else if (user instanceof Business) {
            editor.putString("userType", "business");
        }
        editor.apply();
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }
    public void handleForgotPassword(){
        Intent intent = new Intent(context, ForgotPasswordActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }

    public void handleSignUp() {
        Intent intent = new Intent(context, CreateAccount_SelectUserTypeActivityView.class);
        context.startActivity(intent);
        view.finishActivity();

    }

    private Object[] callSQLLogin(String userEmail, String userPassword) {
        return MySQLConnector.validateEmailPassword(userEmail, userPassword, context);
    }
}
