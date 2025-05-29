package com.example.mandelamoney;

import android.content.Context;

import java.sql.SQLException;

public class LoginController {
    private final Context context;
    private final ILoginView view;
    private User user;
    public LoginController(Context context, ILoginView view) {
        this.context = context;
        this.view = view;

    }

    public void handelLogin(String userEmail, String userPassword) throws SQLException {
        if ((userEmail.length() < 5) || (userPassword.length() < 5))
        {
            view.showErrorMessage();
            return;
        }

        user = callSQL(userEmail, userPassword);

        if (user == null) {
            view.showErrorMessage();
            return;
        }

        view.hideErrorMessage();
        //call dashboard and pass user

    }

    private User callSQL(String userEmail, String userPassword) throws SQLException {
        return MySQLConnector.validateEmailPassword(userEmail, userPassword, context);
    }
}
