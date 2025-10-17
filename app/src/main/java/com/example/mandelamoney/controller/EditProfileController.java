package com.example.mandelamoney.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.Hasher;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.UserValueChecker;
import com.example.mandelamoney.view.Iface.IEditProfileView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class EditProfileController {
    private final IEditProfileView view;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public EditProfileController(IEditProfileView view, Context context) {
        this.view = view;
        this.context = context;
        runOnUiThread(view::loadUser);
    }

    public void handleSaveButton(String param1, String param2, String param3) {
        runOnUiThread(view::hideError);
        runOnUiThread(view::hideError1);
        runOnUiThread(view::hideError2);
        runOnUiThread(view::hideError3);
        runOnUiThread(view::showLoadingScreen);

        Thread thread = new Thread(() -> {
            User user;
            boolean[] errors = new boolean[4];
            for (int i = 0; i < errors.length; i++) {
                errors[i] = false;
            }

            if (UserSession.getUser() instanceof Student) {
                if (param1.length() < 2) {
                    errors[0] = true;
                }
                if (param2.length() < 2) {
                    errors[1] = true;
                }
                if (!UserValueChecker.isValidStudentNumber(param3)) {
                    errors[2] = true;
                }

                for (boolean error : errors) {
                    if (error) {
                        onFailure(errors);
                        return;
                    }
                }

                user = MySQLConnector.updateStudentDetails(
                        UserSession.getUser().getUserEmail(),
                        Hasher.getHash(UserSession.getUser().getUserPassword()),
                        param1, param2, param3, context
                );

            } else {
                if (param1.length() < 2) {
                    errors[0] = true;
                }
                if (!UserValueChecker.isValidPhoneNumber(param2)) {
                    errors[1] = true;
                }
                if (!UserValueChecker.isValidVatNumber(param3)) {
                    errors[2] = true;
                }

                for (boolean error : errors) {
                    if (error) {
                        onFailure(errors);
                        return;
                    }
                }

                user = MySQLConnector.updateBusinessDetails(
                        UserSession.getUser().getUserEmail(),
                        Hasher.getHash(UserSession.getUser().getUserPassword()),
                        param1, param2, param3, context
                );
            }

            if (user != null) {
                onSuccess(user);
            } else {
                errors[3] = true;
                onFailure(errors);
                return;
            }
        });

        thread.start();
    }

    private void onSuccess(User user) {
        boolean strongAuth = UserSession.getUser().getStrongAuth();
        boolean weakAuth = UserSession.getUser().getWeakAuth();
        String userPassword = UserSession.getUser().getUserPassword();
        UserSession.setUser(user);
        UserSession.getUser().setStrongAuth(strongAuth);
        UserSession.getUser().setWeakAuth(weakAuth);
        UserSession.getUser().setUserPassword(userPassword);
        UserSession.saveSession(context);
        runOnUiThread(() -> {
            view.hideLoadingScreen();
            view.finishActivity();
        });
    }

    private void onFailure(boolean[] errors) {
        runOnUiThread(() -> {
            for (int i = 0; i < errors.length; i++) {
                if (errors[i]) {
                    switch(i) {
                        case 0: view.showError1(); break;
                        case 1: view.showError2(); break;
                        case 2: view.showError3(); break;
                        case 3: view.showError(context.getString(R.string.unknown_error_occurred)); break;
                    }
                }
            }
            view.hideLoadingScreen();
        });
    }


    private void runOnUiThread(Runnable task) {
        mainHandler.post(task);
    }

    public void handleCancelButton() {
        runOnUiThread(view::finishActivity);
    }
}
