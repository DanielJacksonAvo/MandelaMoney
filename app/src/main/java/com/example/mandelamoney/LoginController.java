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
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.putExtra("user", user);
        context.startActivity(intent);
    }

    private Object[] callSQLLogin(String userEmail, String userPassword) {
        return MySQLConnector.validateEmailPassword(userEmail, userPassword, context);
    }
}
