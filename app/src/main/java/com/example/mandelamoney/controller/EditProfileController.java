package com.example.mandelamoney.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.UserValueChecker;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditProfileController {
    private final IEditProfileView view;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public EditProfileController(IEditProfileView view, Context context) {
        this.view = view;
        this.context = context;
        runOnUiThread(view::loadUser);
    }

    public void handleSaveButton(String email, String param1, String param2, String param3) {
        runOnUiThread(view::hideError);

        Thread thread = new Thread(() -> {
            User user;

            if (!UserValueChecker.isValidEmail(email)) {
                showErrorOnMainThread("Invalid Email");
                onFailure();
                return;
            }

            if (UserSession.getUser() instanceof Student) {
                if (param1.length() < 2) {
                    showErrorOnMainThread("Enter A First Name");
                    onFailure();
                    return;
                }
                if (param2.length() < 2) {
                    showErrorOnMainThread("Enter A Last Name");
                    onFailure();
                    return;
                }
                if (!UserValueChecker.isValidStudentNumber(param3)) {
                    showErrorOnMainThread("Invalid Student Number");
                    onFailure();
                    return;
                }

                user = MySQLConnector.updateStudentDetails(
                        UserSession.getUser().getUserEmail(),
                        UserSession.getUser().getUserPassword(),
                        email, param1, param2, param3, context
                );

            } else {
                if (param1.length() < 2) {
                    showErrorOnMainThread("Enter A Business Name");
                    onFailure();
                    return;
                }
                if (!UserValueChecker.isValidPhoneNumber(param2)) {
                    showErrorOnMainThread("Invalid Phone Number");
                    onFailure();
                    return;
                }
                if (!UserValueChecker.isValidVatNumber(param3)) {
                    showErrorOnMainThread("Invalid VAT Number");
                    onFailure();
                    return;
                }

                user = MySQLConnector.updateBusinessDetails(
                        UserSession.getUser().getUserEmail(),
                        UserSession.getUser().getUserPassword(),
                        email, param1, param2, param3, context
                );
            }

            if (user != null) {
                onSuccess(user);
            } else {
                onFailure();
            }
        });

        thread.start();
    }

    private void onSuccess(User user) {
        UserSession.setUser(user);
        runOnUiThread(() -> {
            view.hideLoadingScreen();
            view.finishActivity();
        });
    }

    private void onFailure() {
        runOnUiThread(() -> {
            //view.showError("Error Saving");
            view.hideLoadingScreen();
        });
    }

    private void showErrorOnMainThread(String message) {
        runOnUiThread(() -> view.showError(message));
    }

    private void runOnUiThread(Runnable task) {
        mainHandler.post(task);
    }

    public void handleCancelButton() {
        runOnUiThread(view::finishActivity);
    }
}
