package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log; // Import Log for debugging
import android.widget.Toast;

import androidx.core.content.ContextCompat; // For getting main executor

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
import java.util.concurrent.ExecutorService; // New import
import java.util.concurrent.Executors; // New import
import java.util.concurrent.TimeUnit; // New import
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountController {
    private Context context;
    private ISelectUserTypeCreateAccountView viewSelectUserType;
    private ICreateStudentAccountView viewCreateStudentAccount;
    private ICreateBusinessAccountView viewCreateBusinessAccount;

    // New executor for background account creation tasks
    private final ExecutorService accountCreationExecutor = Executors.newSingleThreadExecutor();

    public CreateAccountController(Context context, ISelectUserTypeCreateAccountView viewSelectUserType) {
        this.context = context;
        this.viewSelectUserType = viewSelectUserType;
    }

    public CreateAccountController(Context context, ICreateStudentAccountView viewCreateStudentAccount) {
        this.context = context;
        this.viewCreateStudentAccount = viewCreateStudentAccount;
    }

    public CreateAccountController(Context context, ICreateBusinessAccountView viewCreateBusinessAccount) {
        this.context = context;
        this.viewCreateBusinessAccount = viewCreateBusinessAccount;
    }


    public void handleUserTypeSelection(int userNewType) {
        Intent intent;
        // No need to nullify viewSelectUserType here, as it's passed via constructor
        // and its lifecycle is managed by the activity.
        switch (userNewType) {
            case 0: // Business
                DataShare.send(this);
                intent = new Intent(context, CreateAccountEnterBusinessDetailsActivity.class);
                context.startActivity(intent);
                break;
            case 1: // Student
                DataShare.send(this);
                intent = new Intent(context, CreateAccountEnterStudentDetailsActivity.class);
                context.startActivity(intent);
                break;
            default:
                Log.w("CreateAccountController", "Unknown user type selected: " + userNewType);
        }
    }


    public void handleCreateStudentUser(String userEmail, String userFirstName, String userLastName, String userStudentNumber, String userPassword, String userPasswordReenter) {
        // --- UI Thread Validations (fast, no database calls) ---
        if (viewCreateStudentAccount != null) {
            viewCreateStudentAccount.hidePasswordError();
            viewCreateStudentAccount.hideDetailError();
        }

        if (checkEmpty(userEmail)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_an_email));
            return;
        }

        if (!isValidEmail(userEmail)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showDetailError(context.getString(R.string.invalid_email));
            return;
        }

        if (checkEmpty(userFirstName)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_first_name));
            return;
        }

        if (checkEmpty(userLastName)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_last_name));
            return;
        }

        if (checkEmpty(userStudentNumber)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showDetailError(context.getString(R.string.enter_a_student_number));
            return;
        }

        if (!isValidStudentNumber(userStudentNumber)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showDetailError(context.getString(R.string.invalid_student_number));
            return;
        }

        if (checkEmpty(userPassword)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showPasswordError(context.getString(R.string.enter_a_password));
            return;
        }

        if (!checkPasswordLength(userPassword)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showPasswordError(context.getString(R.string.password_too_short));
            return;
        }

        if (!checkPasswordMatch(userPassword, userPasswordReenter)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showPasswordError(context.getString(R.string.password_mismatch));
            return;
        }

        // --- Background Thread for Database Operations ---
        accountCreationExecutor.execute(() -> {
            boolean accountCreated = false;
            String errorMessage = null;

            // Database call: Check unique email
            if (MySQLConnector.checkUniqueEmail(userEmail, context)) {
                errorMessage = context.getString(R.string.email_already_in_use);
            } else {
                // Database call: Create student account
                accountCreated = MySQLConnector.createStudentAccount(userEmail, userPassword, userFirstName, userLastName, userStudentNumber, context);
                if (!accountCreated) {
                    errorMessage = "Account Failed to Create!"; // More specific error if needed
                }
            }

            // --- Post Results to Main Thread ---
            final boolean finalAccountCreated = accountCreated;
            final String finalErrorMessage = errorMessage;
            ContextCompat.getMainExecutor(context).execute(() -> {
                if (finalAccountCreated) {
                    Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();
                    handleCreateStudentAccountCancel(); // Navigates to LoginActivity and finishes current
                } else {
                    Toast.makeText(context, finalErrorMessage != null ? finalErrorMessage : "An unexpected error occurred!", Toast.LENGTH_LONG).show();
                    if (viewCreateStudentAccount != null) {
                        // You might want to show the error on the view as well, depending on your UI design
                        viewCreateStudentAccount.showDetailError(finalErrorMessage != null ? finalErrorMessage : "An unexpected error occurred!");
                    }
                }
            });
        });
    }

    public void handleCreateBusinessUser(String userEmail, String userBusinessName, String userBusinessVAT, String userBusinessPhone, String userPassword, String userPasswordReenter) {
        // --- UI Thread Validations (fast, no database calls) ---
        if (viewCreateBusinessAccount != null) {
            viewCreateBusinessAccount.hidePasswordError();
            viewCreateBusinessAccount.hideDetailError();
        }

        if (checkEmpty(userEmail)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showDetailError(context.getString(R.string.enter_an_email));
            return;
        }

        if (!isValidEmail(userEmail)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showDetailError(context.getString(R.string.invalid_email));
            return;
        }

        if (checkEmpty(userBusinessName)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showDetailError(context.getString(R.string.enter_a_business_name));
            return;
        }

        if (checkEmpty(userBusinessVAT)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showDetailError(context.getString(R.string.enter_a_vat_number));
            return;
        }

        if (!isValidVatNumber(userBusinessVAT)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showDetailError(context.getString(R.string.invalid_vat_number));
            return;
        }

        // Note: The original code had a bug here: if (isValidPhoneNumber(userBusinessPhone))
        // It should be if (!isValidPhoneNumber(userBusinessPhone)) to show error for invalid phone number
        if (!isValidPhoneNumber(userBusinessPhone)) { // Corrected logic
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showDetailError(context.getString(R.string.invalid_phone_number));
            return;
        }

        if (checkEmpty(userPassword)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showPasswordError(context.getString(R.string.enter_a_password));
            return;
        }

        if (!checkPasswordLength(userPassword)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showPasswordError(context.getString(R.string.password_too_short));
            return;
        }

        if (!checkPasswordMatch(userPassword, userPasswordReenter)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showPasswordError(context.getString(R.string.password_mismatch));
            return;
        }

        // --- Background Thread for Database Operations ---
        accountCreationExecutor.execute(() -> {
            boolean accountCreated = false;
            String errorMessage = null;

            // Database call: Check unique email
            if (MySQLConnector.checkUniqueEmail(userEmail, context)) {
                errorMessage = context.getString(R.string.email_already_in_use);
            } else {
                // Database call: Create business account
                accountCreated = MySQLConnector.createBusinessAccount(userEmail, userPassword, userBusinessName, userBusinessPhone, userBusinessVAT, context);
                if (!accountCreated) {
                    errorMessage = "Account Failed to Create!"; // More specific error if needed
                }
            }

            // --- Post Results to Main Thread ---
            final boolean finalAccountCreated = accountCreated;
            final String finalErrorMessage = errorMessage;
            ContextCompat.getMainExecutor(context).execute(() -> {
                if (finalAccountCreated) {
                    Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();
                    handleCreateBusinessAccountCancel(); // Navigates to LoginActivity and finishes current
                } else {
                    Toast.makeText(context, finalErrorMessage != null ? finalErrorMessage : "An unexpected error occurred!", Toast.LENGTH_LONG).show();
                    if (viewCreateBusinessAccount != null) {
                        // You might want to show the error on the view as well
                        viewCreateBusinessAccount.showDetailError(finalErrorMessage != null ? finalErrorMessage : "An unexpected error occurred!");
                    }
                }
            });
        });
    }

    public void handleSelectUserTypeCancel() {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        if (viewSelectUserType != null) {
            viewSelectUserType.finishActivity();
        }
    }

    public void handleCreateStudentAccountCancel() {
        DataShare.send(this); // Ensure DataShare is still needed after navigation
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        if (viewCreateStudentAccount != null) {
            viewCreateStudentAccount.finishActivity();
        }
    }

    public void handleCreateBusinessAccountCancel() {
        DataShare.send(this); // Ensure DataShare is still needed after navigation
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        if (viewCreateBusinessAccount != null) {
            viewCreateBusinessAccount.finishActivity();
        }
    }

    // New constructors to properly set the specific view interfaces
    public void setContextViewStudent(Context context, ICreateStudentAccountView viewCreateStudentAccount) {
        this.context = context;
        this.viewCreateStudentAccount = viewCreateStudentAccount;
        this.viewCreateBusinessAccount = null; // Ensure only one view type is active
        this.viewSelectUserType = null;
    }

    public void setContextViewBusiness(Context context, ICreateBusinessAccountView viewCreateBusinessAccount) {
        this.context = context;
        this.viewCreateBusinessAccount = viewCreateBusinessAccount;
        this.viewCreateStudentAccount = null; // Ensure only one view type is active
        this.viewSelectUserType = null;
    }

    private boolean checkPasswordMatch(String userPassword, String userPasswordReenter) {
        return Objects.equals(userPassword, userPasswordReenter);
    }

    private boolean checkEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private boolean checkPasswordLength(String userPassword) {
        return userPassword.length() >= 8;
    }

    // This method will now be called from the background thread
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
        if (studentNumber == null) return false; // Added null check
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

    // Cleanup method to shut down the executor
    public void cleanup() {
        if (accountCreationExecutor != null && !accountCreationExecutor.isShutdown()) {
            accountCreationExecutor.shutdown();
            try {
                if (!accountCreationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    accountCreationExecutor.shutdownNow();
                }
            } catch (InterruptedException ie) {
                accountCreationExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        Log.d("CreateAccountController", "Account creation executor cleaned up.");
    }
}