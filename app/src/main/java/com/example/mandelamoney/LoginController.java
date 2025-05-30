package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;

import java.sql.SQLException;

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

        user = callSQLLogin(userEmail, userPassword);

        if (user == null) {
            view.showErrorMessage();
            return;
        }

        view.hideErrorMessage();
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.putExtra("user", user);
        context.startActivity(intent);
    }

    private User callSQLLogin(String userEmail, String userPassword) {
        return MySQLConnector.validateEmailPassword(userEmail, userPassword, context);
    }
}
