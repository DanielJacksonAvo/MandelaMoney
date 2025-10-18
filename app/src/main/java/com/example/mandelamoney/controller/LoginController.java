package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.LoginManager;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.UserValueChecker;
import com.example.mandelamoney.view.Iface.ILoginView;
import com.example.mandelamoney.view.activity.CreateAccountSelectUserTypeActivity;
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.ForgotPasswordActivity;
import com.example.mandelamoney.view.activity.UnlockActivity;

import java.sql.SQLException;


public class LoginController {
    private final Context context;
    private final ILoginView view;

    public LoginController(Context context, ILoginView view) {
        this.context = context;
        this.view = view;
        checkForExistingSession();

    }

    public void handleLogin(String userEmail, String userPassword) {
        view.showLoadingSpinner();
        view.hideErrorMessage();
        view.hideEmailErrorMessage();
        view.hidePasswordError();
        boolean error = false;
        if (UserValueChecker.checkEmpty(userEmail)) {
            view.hideLoadingSpinner();
            view.showEmailErrorMessage(context.getString(R.string.enter_an_email));
            error = true;
        } else {
            if (!UserValueChecker.isValidEmail(userEmail)) {
                view.hideLoadingSpinner();
                view.showEmailErrorMessage(context.getString(R.string.invalid_email));
                error = true;
            }
        }
        if (UserValueChecker.checkEmpty(userPassword)) {
            view.hideLoadingSpinner();
            view.showPasswordError();
            error = true;
        }
        if (error) {
            return;
        }

        LoginManager.login(context, userEmail, userPassword,
                this::onSuccess
                ,this::onFailure
        );
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

    private void onSuccess() {
        view.hideLoadingSpinner();
        view.hideErrorMessage();
        view.hideEmailErrorMessage();
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }

    private void onFailure() {
        view.hideLoadingSpinner();
        view.showErrorMessage();
    }

    private void checkForExistingSession() {
        if (UserSession.isSessionExists(context)) {
            Intent intent = new Intent(context, UnlockActivity.class);
            context.startActivity(intent);
            view.finishActivity();
        }

    }


}