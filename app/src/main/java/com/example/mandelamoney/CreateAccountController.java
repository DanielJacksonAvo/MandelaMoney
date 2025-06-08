package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class CreateAccountController {
    private int userType;
    private final Context context;
    private final ISelectUserType_CreateAccount viewSelectUserType;

    public CreateAccountController(Context context, ISelectUserType_CreateAccount viewSelectUserType) {
        this.context = context;
        this.viewSelectUserType = viewSelectUserType;
    }

    public void handleUserTypeSelection(int userType) {
        this.userType = userType;
        Intent intent;
        switch (userType) {
            case 0:
                intent = new Intent(context, CreateAccount_EnterBusinessDetailsActivity.class);
                context.startActivity(intent);
                break;
            case 1:
                intent = new Intent(context, CreateAccount_EnterStudentDetailsActivity.class);
                context.startActivity(intent);
                break;
            default:
                return;
        }
    }

    public void handleCreateStudentUser(String userEmail, String userFirstName, String userLastName, String userStudentNumber, String userPassword, String userPasswordReenter) {
        if (!checkPasswordNotEmpty(userPassword)){
            return;
        }

        if (!checkPasswordLength(userPassword)) {
            return;
        }

        if (!checkPasswordMatch(userPassword, userPasswordReenter)) {
            return;
        }
        Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();


    }

    public void handleCreateBusinessUser(String userEmail, String userBusinessName, String userBusinessVAT, String userBusinessPhone, String userPassword, String userPasswordReenter) {

    }

    public void handleCancel() {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        viewSelectUserType.finishActivity();
    }

    private boolean checkPasswordMatch(String userPassword, String userPasswordReenter) {
        /// display error message
        return Objects.equals(userPassword, userPasswordReenter);
    }

    private boolean checkPasswordNotEmpty(String userPassword) {
        /// display error message
        return userPassword != null;
    }

    private boolean checkPasswordLength(String userPassword) {
        /// display error message
        return userPassword.length() >= 8;
    }

    private void checkUniqueEmail(String userEmail) {

    }


}
