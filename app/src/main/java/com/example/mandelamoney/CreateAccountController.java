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
    private ICreateBusinessAccount viewCreateBusinessAccount;

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
        viewCreateStudentAccount.hideDetailError();
        if (checkEmpty(userEmail)) {
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

        if (checkEmpty(userFirstName)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_first_name));
            return;
        }

        if (checkEmpty(userLastName)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_last_name));
            return;
        }

        if (checkEmpty(userStudentNumber)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_student_number));
            return;
        }

        if (!isValidStudentNumber(userStudentNumber)) {
            viewCreateStudentAccount.showDetailError(context.getString(R.string.invalid_student_number));
            return;
        }

        if (checkEmpty(userPassword)) {
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
        viewCreateStudentAccount.hideDetailError();

        if(MySQLConnector.createStudentAccount(userEmail, userPassword, userFirstName, userLastName, userStudentNumber, context) == false) {
            Toast.makeText(context, "Account Failed to Created!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();
    }

    public void handleCreateBusinessUser(String userEmail, String userBusinessName, String userBusinessVAT, String userBusinessPhone, String userPassword, String userPasswordReenter) {
        viewCreateBusinessAccount.hidePasswordError();
        viewCreateBusinessAccount.hideDetailError();

        if (checkEmpty(userEmail)) {
            viewCreateBusinessAccount.showDetailError(context.getString(R.string.enter_an_email));
            return;
        }

        if (!isValidEmail(userEmail)) {
            viewCreateBusinessAccount.showDetailError(context.getString(R.string.invalid_email));
            return;
        }

        if (checkUniqueEmail(userEmail)) {
            viewCreateBusinessAccount.showDetailError(context.getString(R.string.email_already_in_use));
            return;
        }

        if (checkEmpty(userBusinessName)) {
            viewCreateBusinessAccount.showDetailError(context.getString(R.string.enter_a_business_name));
            return;
        }

        if (checkEmpty(userBusinessVAT)) {
            viewCreateBusinessAccount.showDetailError(context.getString(R.string.enter_a_vat_number));
            return;
        }

        if (!isValidVatNumber(userBusinessVAT)) {
            viewCreateBusinessAccount.showDetailError(context.getString(R.string.invalid_vat_number));
            return;
        }

        if (isValidPhoneNumber(userBusinessPhone)) {
            viewCreateBusinessAccount.showDetailError(context.getString(R.string.invalid_phone_number));
        }

        if (checkEmpty(userPassword)) {
            viewCreateBusinessAccount.showPasswordError(context.getString(R.string.enter_a_password));
            return;
        }

        if (!checkPasswordLength(userPassword)) {
            viewCreateBusinessAccount.showPasswordError(context.getString(R.string.password_too_short));
            return;
        }

        if (!checkPasswordMatch(userPassword, userPasswordReenter)) {
            viewCreateBusinessAccount.showPasswordError(context.getString(R.string.password_mismatch));
            return;
        }

        viewCreateBusinessAccount.hidePasswordError();
        viewCreateBusinessAccount.hideDetailError();

        if(MySQLConnector.createBusinessAccount(userEmail, userPassword, userBusinessName, userBusinessPhone, userBusinessVAT, context) == false) {
            Toast.makeText(context, "Account Failed to Created!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();

    }

    public void handleSelectUserTypeCancel() {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        viewSelectUserType.finishActivity();
    }

    public void handleCreateAccountCancel() {
        DataShare.send(this);
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        viewSelectUserType.finishActivity();

    }


    public void setContextViewStudent(Context context, ICreateStudentAccount viewCreateStudentAccount) {
        this.context = context;
        this.viewCreateStudentAccount = viewCreateStudentAccount;
    }

    public void setContextViewBusiness(Context context, ICreateBusinessAccount viewCreateBusinessAccount) {
        this.context = context;
        this.viewCreateBusinessAccount = viewCreateBusinessAccount;
    }

    private boolean checkPasswordMatch(String userPassword, String userPasswordReenter) {
        return Objects.equals(userPassword, userPasswordReenter);
    }


    private boolean checkEmpty(String s) {
        if (s == null) {
            return true;
        }

        if (s.isEmpty()) {
            return true;
        }

        if (s.equals("")) {
            return true;
        }

        return false;
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

    private boolean isValidVatNumber(String vatNumber) {
        return vatNumber != null && vatNumber.matches("^\\d{10}$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^0\\d{9}$");
    }

}
