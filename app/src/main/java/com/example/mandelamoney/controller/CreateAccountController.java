package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log; // Import Log for debugging
import android.widget.Toast;

import androidx.core.content.ContextCompat; // For getting main executor

import com.example.mandelamoney.R;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.Hasher;
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
            viewCreateStudentAccount.showLoadingSpinner();
            viewCreateStudentAccount.hidePasswordError();
            viewCreateStudentAccount.hideEmailError();
            viewCreateStudentAccount.hideFirstNameError();
            viewCreateStudentAccount.hideLastNameError();
            viewCreateStudentAccount.hideStudentNumberError();
        }

        boolean error = false;

        if (checkEmpty(userEmail)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showEmailError(context.getString(R.string.enter_an_email));
            error = true;
        } else {
            if (!isValidEmail(userEmail)) {
                if (viewCreateStudentAccount != null) viewCreateStudentAccount.showEmailError(context.getString(R.string.invalid_email));
                error = true;

            }
        }



        if (checkEmpty(userFirstName)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showFirstNameError();
            error = true;

        }

        if (checkEmpty(userLastName)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showLastNameError();
            error = true;

        }

        if (checkEmpty(userStudentNumber) || !isValidStudentNumber(userStudentNumber)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showStudentNumberError();
            error = true;

        }
        userStudentNumber = ensureStartsWithS(userStudentNumber);

        if (checkEmpty(userPassword)) {
            if (viewCreateStudentAccount != null) viewCreateStudentAccount.showPasswordError(context.getString(R.string.enter_a_password), true);
            error = true;

        } else {
            if (!checkPasswordLength(userPassword)) {
                if (viewCreateStudentAccount != null) viewCreateStudentAccount.showPasswordError(context.getString(R.string.password_too_short), true);
                error = true;

            } else {
                if (!checkPasswordMatch(userPassword, userPasswordReenter)) {
                    if (viewCreateStudentAccount != null) viewCreateStudentAccount.showPasswordError(context.getString(R.string.password_mismatch), true);
                    error = true;

                }
            }
        }

        if (error) {
            assert viewCreateStudentAccount != null;
            viewCreateStudentAccount.hideLoadingSpinner();
            return;
        }

        // --- Background Thread for Database Operations ---
        String finalUserStudentNumber = userStudentNumber;
        accountCreationExecutor.execute(() -> {
            boolean accountCreated;

            // Database call: Check unique email
            if (!MySQLConnector.checkUniqueEmail(userEmail, context)) {
                ContextCompat.getMainExecutor(context).execute(() -> {
                    viewCreateStudentAccount.showEmailError(context.getString(R.string.email_already_in_use));
                    viewCreateStudentAccount.hideLoadingSpinner();
                });
                return;
            } else {
                // Database call: Create student account
                accountCreated = MySQLConnector.createStudentAccount(userEmail, Hasher.getHash(userPassword), userFirstName, userLastName, finalUserStudentNumber, context);
            }

            // --- Post Results to Main Thread ---
            boolean finalAccountCreated = accountCreated;
            ContextCompat.getMainExecutor(context).execute(() -> {
                if (finalAccountCreated) {
                    Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();
                    handleCreateStudentAccountCancel(); // Navigates to LoginActivity and finishes current
                } else {
                    if (viewCreateStudentAccount != null) {
                        // You might want to show the error on the view as well, depending on your UI design
                        viewCreateStudentAccount.showPasswordError(context.getString(R.string.unknown_error_occurred), false);
                    }
                }
                assert viewCreateStudentAccount != null;
                viewCreateStudentAccount.hideLoadingSpinner();

            });
        });
    }

    public void handleCreateBusinessUser(String userEmail, String userBusinessName, String userBusinessVAT, String userBusinessPhone, String userPassword, String userPasswordReenter) {
        // --- UI Thread Validations (fast, no database calls) ---
        viewCreateBusinessAccount.hidePasswordError();
        viewCreateBusinessAccount.hideEmailError();
        viewCreateBusinessAccount.hideBusinessNameError();
        viewCreateBusinessAccount.hideVATError();
        viewCreateBusinessAccount.hidePhoneError();
        viewCreateBusinessAccount.showLoadingSpinner();


        boolean error = false;

        if (checkEmpty(userEmail)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showEmailError(context.getString(R.string.enter_an_email));
            error = true;
        } else {
            if (!isValidEmail(userEmail)) {
                if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showEmailError(context.getString(R.string.invalid_email));
                error = true;
            }
        }



        if (checkEmpty(userBusinessName)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showBusinessNameError();
            error = true;
        }

        if (checkEmpty(userBusinessVAT) || !isValidVatNumber(userBusinessVAT)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showVATError();
            error = true;
        }

        // Note: The original code had a bug here: if (isValidPhoneNumber(userBusinessPhone))
        // It should be if (!isValidPhoneNumber(userBusinessPhone)) to show error for invalid phone number
        if (checkEmpty(userBusinessPhone) || !isValidPhoneNumber(userBusinessPhone)) { // Corrected logic
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showPhoneError();
            error = true;
        }

        if (checkEmpty(userPassword)) {
            if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showPasswordError(context.getString(R.string.enter_a_password), true);
            error = true;
        } else {
            if (!checkPasswordLength(userPassword)) {
                if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showPasswordError(context.getString(R.string.password_too_short), true);
                error = true;
            } else {
                if (!checkPasswordMatch(userPassword, userPasswordReenter)) {
                    if (viewCreateBusinessAccount != null) viewCreateBusinessAccount.showPasswordError(context.getString(R.string.password_mismatch), true);
                    error = true;
                }
            }
        }


        if (error) {
            assert viewCreateBusinessAccount != null;
            viewCreateBusinessAccount.hideLoadingSpinner();
            return;
        }



        // --- Background Thread for Database Operations ---
        accountCreationExecutor.execute(() -> {
            boolean accountCreated;

            // Database call: Check unique email
            if (!MySQLConnector.checkUniqueEmail(userEmail, context)) {
                ContextCompat.getMainExecutor(context).execute(() -> {
                    viewCreateBusinessAccount.showEmailError(context.getString(R.string.email_already_in_use));
                    viewCreateBusinessAccount.hideLoadingSpinner();
                });
                return;
            } else {
                // Database call: Create business account
                accountCreated = MySQLConnector.createBusinessAccount(userEmail, Hasher.getHash(userPassword), userBusinessName, userBusinessPhone, userBusinessVAT, context);
            }

            // --- Post Results to Main Thread ---
            final boolean finalAccountCreated = accountCreated;
            ContextCompat.getMainExecutor(context).execute(() -> {
                if (finalAccountCreated) {
                    Toast.makeText(context, "Account Successfully Created!\nPlease login.", Toast.LENGTH_LONG).show();
                    handleCreateBusinessAccountCancel(); // Navigates to LoginActivity and finishes current
                } else {
                    if (viewCreateBusinessAccount != null) {
                        viewCreateBusinessAccount.showPasswordError(context.getString(R.string.unknown_error_occurred), false);
                    }
                }
                assert viewCreateBusinessAccount != null;
                viewCreateBusinessAccount.hideLoadingSpinner();
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
        try {
            if (viewCreateStudentAccount != null) {
                viewCreateStudentAccount.finishActivity();
            }
            if (viewSelectUserType != null) {
                viewSelectUserType.finishActivity();
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    public void handleCreateBusinessAccountCancel() {
        DataShare.send(this); // Ensure DataShare is still needed after navigation
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        try {
            if (viewCreateBusinessAccount != null) {
                viewCreateBusinessAccount.finishActivity();
            }
            if (viewSelectUserType != null) {
                viewSelectUserType.finishActivity();
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    public void setContextViewStudent(Context context, ICreateStudentAccountView viewCreateStudentAccount) {
        this.context = context;
        this.viewCreateStudentAccount = viewCreateStudentAccount;
    }

    public void setContextViewBusiness(Context context, ICreateBusinessAccountView viewCreateBusinessAccount) {
        this.context = context;
        this.viewCreateBusinessAccount = viewCreateBusinessAccount;
    }

    public String ensureStartsWithS(String input) {
        String lowerInput = input.toLowerCase();
        return lowerInput.startsWith("s") ? lowerInput : "s" + lowerInput;
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

}
