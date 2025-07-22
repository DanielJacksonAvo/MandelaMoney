package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.ILoginView;
import com.example.mandelamoney.view.activity.CreateAccountSelectUserTypeActivity;
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.ForgotPasswordActivity;

import java.sql.SQLException;


public class LoginController {
    private final Context context;
    private final ILoginView view;

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

        User user = (User) objs[0];

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
        Intent intent = new Intent(context, CreateAccountSelectUserTypeActivity.class);
        context.startActivity(intent);
        view.finishActivity();

    }

    private Object[] callSQLLogin(String userEmail, String userPassword) {
        return MySQLConnector.validateEmailPassword(userEmail, userPassword, context);
    }
}
