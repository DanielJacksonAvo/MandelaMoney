package com.example.mandelamoney;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountController {
    private int userType;
    private Context context;
    private ISelectUserType_CreateAccount viewSelectUserType;
    private ICreateStudentAccount viewCreateStudentAccount;

    public CreateAccountController(Context context, ISelectUserType_CreateAccount viewSelectUserType) {
        this.context = context;
        this.viewSelectUserType = viewSelectUserType;
    }

    public void handleUserTypeSelection(int userType) {
        this.userType = userType;
        Intent intent;
        this.viewSelectUserType = null;
        switch (userType) {
            case 0:
                DataShare.send(this);
                intent = new Intent(context, CreateAccount_EnterBusinessDetailsActivity.class);
                context.startActivity(intent);
                break;
            case 1:
                DataShare.send(this);
                intent = new Intent(context, CreateAccount_EnterStudentDetailsActivity.class);
                context.startActivity(intent);
                break;
            default:
                return;
        }
    }

    public void handleCreateStudentUser(String userEmail, String userFirstName, String userLastName, String userStudentNumber, String userPassword, String userPasswordReenter) {
        viewCreateStudentAccount.hidePasswordError();
        if (!checkNotEmpty(userEmail)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_an_email));
            return;
        }

        if (!isValidEmail(userEmail)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.invalid_email));
            return;
        }

        if (checkUniqueEmail(userEmail)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.email_already_in_use));
            return;
        }

        if (!checkNotEmpty(userFirstName)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_first_name));
            return;
        }

        if(!checkNotEmpty(userLastName)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_last_name));
            return;
        }

        if (!checkNotEmpty(userStudentNumber)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_student_number));
            return;
        }

        if (!isValidStudentNumber(userStudentNumber)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.invalid_student_number));
            return;
        }

        if (!checkNotEmpty(userPassword)) {
            viewCreateStudentAccount.showPasswordError(context.getString(R.string.enter_a_password));
            return;
        }

        if (!checkPasswordLength(userPassword)) {
            viewCreateStudentAccount.showPasswordError(context.getString(R.string.password_too_short));
            return;
        }

        if (!checkPasswordMatch(userPassword, userPasswordReenter)) {
            viewCreateStudentAccount.showPasswordError(context.getString(R.string.password_mismatch));
            return;
        }

        viewCreateStudentAccount.hidePasswordError();
        Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();
    }

    public void handleCreateBusinessUser(String userEmail, String userBusinessName, String userBusinessVAT, String userBusinessPhone, String userPassword, String userPasswordReenter) {

    }

    public void handleSelectUserTypeCancel() {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        viewSelectUserType.finishActivity();
    }

    public void handleCreateStudentAccountCancel() {
        DataShare.send(this);
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        viewSelectUserType.finishActivity();

    }

    public void setContextViewStudent(Context context, ICreateStudentAccount viewCreateStudentAccount){
        this.context = context;
        this.viewCreateStudentAccount = viewCreateStudentAccount;
    }

    private boolean checkPasswordMatch(String userPassword, String userPasswordReenter) {
        return Objects.equals(userPassword, userPasswordReenter);
    }


    private boolean checkNotEmpty(String s) {
        if (s == null) {
            return false;
        }

        if (s.isEmpty()) {
            return false;
        }

        if (s.equals("")){
            return false;
        }

        return true;
    }

    private boolean checkPasswordLength(String userPassword) {
        return userPassword.length() >= 8;
    }

    private boolean checkUniqueEmail(String userEmail) {
        return MySQLConnector.checkUniqueEmail(userEmail, context);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidStudentNumber(String studentNumber) {
        if (studentNumber.charAt(0) != 's') {
            studentNumber = "s" + studentNumber;
        }
        String regex = "^s\\d{9}$";
        return studentNumber.matches(regex);
    }

}
