package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.BiometricsManager;
import com.example.mandelamoney.util.LoginManager;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IUnlockView;
import com.example.mandelamoney.view.activity.DashboardActivity;
import com.example.mandelamoney.view.activity.LoginActivity;
import com.example.mandelamoney.view.activity.UnlockActivity;

public class UnlockController {
    private Context context;
    private IUnlockView view;

    public UnlockController(Context context, IUnlockView view) {
        this.context = context;
        this.view = view;
        UserSession.loadSession(context);
    }

    public void handleBiometrics() {

        BiometricsManager.authenticate(
                (UnlockActivity)context,
                () -> {
                    view.showLoadingSpinner();
                    Toast.makeText(context, "Authenticated successfully!", Toast.LENGTH_SHORT).show();
                    if (UserSession.getUser() == null) {
                        Toast.makeText(context, "Session Expired", Toast.LENGTH_LONG).show();
                        handleLogout();
                        return;
                    }
                    LoginManager.login(context, UserSession.getUser().getUserEmail(), UserSession.getUser().getUserPassword(),
                            this::onSuccess,
                            this::onFailure
                    );
                },
                () -> {
                    Toast.makeText(context, "Authentication failed or cancelled.", Toast.LENGTH_SHORT).show();
                }
        );
    }

    public void handleLogout() {
        UserSession.deleteSession(context);
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }

    public void handleUnlock(String userPassword) {
        view.showLoadingSpinner();
        view.hideErrorMessage();
        UserSession.loadSession(context);
        User user = UserSession.getUser();
        if (user == null) {
            handleLogout();
            return;
        }
        LoginManager.login(context, user.getUserEmail(), userPassword,
                () -> {
                    if (!userPassword.equals(user.getUserPassword())) {
                        Toast.makeText(context, "Session Expired", Toast.LENGTH_LONG).show();
                        handleLogout();
                    } else {
                        onSuccess();
                    }

                },
                () -> {
                    onFailure();
                    view.showErrorMessage();
                }
        );
    }


    private void onSuccess() {
        view.hideLoadingSpinner();
        view.hideErrorMessage();
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }

    private void onFailure() {
        view.hideLoadingSpinner();
    }


}
