package com.example.mandelamoney.controller;


import android.content.Context;

import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.UserValueChecker;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditProfileController {
    private final IEditProfileView view;
    private final Context context;
    public EditProfileController(IEditProfileView view, Context context) {
        this.view = view;
        this.context = context;
        view.loadUser();
    }

    public void handleSaveButton(String email, String param1 /* firstname, business name */, String param2 /* lastname, phone number */, String param3 /* student number, vat number */) {
        view.hideError();
        Thread thread = new Thread(() -> {
            User user;
            if (!UserValueChecker.isValidEmail(email)) {
                view.showError("Invalid Email");
                return;
            }

            if (UserSession.getUser() instanceof Student) {
                if((param1.length() < 2)) {
                    view.showError("Enter A First Name");
                    return;
                }
                if ((param2.length() < 2)) {
                    view.showError("Enter A Last Name");
                    return;
                }
                if (!UserValueChecker.isValidStudentNumber(param3)) {
                    view.showError("Invalid Student Number");
                    return;
                }
                user = MySQLConnector.updateStudentDetails(UserSession.getUser().getUserEmail(), UserSession.getUser().getUserPassword(), email, param1, param2, param3, context);

            } else {
                if (param1.length() < 2) {
                    view.showError("Enter A Business Name");
                    return;
                }
                if (!UserValueChecker.isValidPhoneNumber(param2)) {
                    view.showError("Invalid Phone Number");
                    return;
                }
                if (!UserValueChecker.isValidVatNumber(param3)) {
                    view.showError("Invalid VAT Number");
                    return;
                }
                user = MySQLConnector.updateBusinessDetails(UserSession.getUser().getUserEmail(), UserSession.getUser().getUserPassword(), email, param1, param2, param3, context);
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
        view.hideLoadingScreen();
        view.finishActivity();
    }

    private void onFailure() {
        view.hideLoadingScreen();
    }
    public void handleCancelButton() {
        view.finishActivity();
    }

}
