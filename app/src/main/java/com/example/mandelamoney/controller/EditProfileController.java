package com.example.mandelamoney.controller;


import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.UserValueChecker;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditProfileController {
    private IEditProfileView view;
    public EditProfileController() {

    }

    public void handleSaveButton(String email, String password, String param1 /* firstname, business name */, String param2 /* lastname, phone number */, String param3 /* student number, vat number */) {

        if (!UserValueChecker.isValidEmail(email)) {
            return;
        }

        if (!UserValueChecker.checkPasswordLength(password)) {
            return;
        }

        if (!UserValueChecker.checkPasswordMatch(password, (UserSession.getUser()).getUserPassword())) {
            return;
        }

        if (UserValueChecker.checkEmpty(param1)) {
            return;
        }

        if (UserValueChecker.checkEmpty(param2)) {
            return;
        }

        if (UserValueChecker.checkEmpty(param3)) {
            return;
        }

        if (UserSession.getUser() instanceof Student) {

        } else {

        }



    }
    public void handleCancelButton() {

    }
}
