package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.example.mandelamoney.util.BiometricsManager;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IUnlockView;
import com.example.mandelamoney.view.activity.LoginActivity;
import com.example.mandelamoney.view.activity.UnlockActivity;

public class UnlockController {
    private Context context;
    private IUnlockView view;

    public UnlockController(Context context, IUnlockView view) {
        this.context = context;
        this.view = view;
    }

    public void handleBiometrics() {

        BiometricsManager.authenticate(
                (UnlockActivity)context,
                () -> {
                    // Auth success
                    Toast.makeText(context, "Authenticated successfully!", Toast.LENGTH_SHORT).show();
                },
                () -> {
                    // Auth failure
                    Toast.makeText(context, "Authentication failed or cancelled.", Toast.LENGTH_SHORT).show();
                }
        );
    }

    public void handleLogout() {
        UserSession.saveSession(context);
        UserSession.clearSession();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        view.finishActivity();
    }

    public void handleUnlock(String userPassword) {

    }

    private void loadUserSession() {
        UserSession.loadSession(context);
    }


}
