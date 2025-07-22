package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.mandelamoney.R;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.ICreateBusinessAccountView;
import com.example.mandelamoney.view.Iface.ICreateStudentAccountView;
import com.example.mandelamoney.view.Iface.ISelectUserTypeCreateAccountView;
import com.example.mandelamoney.view.activity.CreateAccountEnterBusinessDetailsActivity;
import com.example.mandelamoney.view.activity.CreateAccountEnterStudentDetailsActivity;
import com.example.mandelamoney.view.activity.LoginActivity;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountController {
    private Context context;
    private ISelectUserTypeCreateAccountView viewSelectUserType;
    private ICreateStudentAccountView viewCreateStudentAccount;
    private ICreateBusinessAccountView viewCreateBusinessAccount;

    public CreateAccountController(Context context, ISelectUserTypeCreateAccountView viewSelectUserType) {
        this.context = context;
        this.viewSelectUserType = viewSelectUserType;
    }

    public void handleUserTypeSelection(int userNewType) {
        Intent intent;
        this.viewSelectUserType = null;
        switch (userNewType) {
            case 0:
                DataShare.send(this);
                intent = new Intent(context, CreateAccountEnterBusinessDetailsActivity.class);
                context.startActivity(intent);
                break;
            case 1:
                DataShare.send(this);
                intent = new Intent(context, CreateAccountEnterStudentDetailsActivity.class);
                context.startActivity(intent);
                break;
            default:
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

        if(!MySQLConnector.createStudentAccount(userEmail, userPassword, userFirstName, userLastName, userStudentNumber, context)) {
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

        if(!MySQLConnector.createBusinessAccount(userEmail, userPassword, userBusinessName, userBusinessPhone, userBusinessVAT, context)) {
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

    public void handleCreateStudentAccountCancel() {
        DataShare.send(this);
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        viewCreateStudentAccount.finishActivity();

    }

    public void handleCreateBusinessAccountCancel() {
        DataShare.send(this);
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        viewCreateBusinessAccount.finishActivity();

    }


    public void setContextViewStudent(Context context, ICreateStudentAccountView viewCreateStudentAccount) {
        this.context = context;
        this.viewCreateStudentAccount = viewCreateStudentAccount;
    }

    public void setContextViewBusiness(Context context, ICreateBusinessAccountView viewCreateBusinessAccount) {
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

        return s.isEmpty();
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
