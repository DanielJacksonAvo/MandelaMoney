package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.mandelamoney.R;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IDepositFundsView;
import com.example.mandelamoney.util.MySQLConnector;
import androidx.core.content.ContextCompat;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.view.Iface.IConfirmDepositView;
import com.example.mandelamoney.view.activity.ConfirmDepositActivity;
import com.example.mandelamoney.view.activity.ShowSuccessActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DepositFundsController {
    private Context context;
    private IDepositFundsView viewDepositFunds;
    private final ExecutorService depositFundsExecutor = Executors.newSingleThreadExecutor();
    private int transactionId;
    private float transactionAmount;
    private String rawAccountNumber;  // sanitized digits used to build masked view
    private User toUserDetails;       // current (logged-in) user
    private IConfirmDepositView confirmDepositView;

    private static final String[] VALID_BANKS = {
            "ABSA",
            "Capitec",
            "Discovery Bank",
            "FNB",
            "Investec",
            "Nedbank",
            "Standard Bank",
            "TymeBank",
            "African Bank"
    };


    public DepositFundsController(Context context, IDepositFundsView viewDepositFunds){
        this.context = context;
        this.viewDepositFunds = viewDepositFunds;
    }
    public void handleDepositFunds(Float amount, String bankName, String branchCode, String cardNumber, String name, String expiryDate, String cvv) {
        if (viewDepositFunds != null) {
            viewDepositFunds.hideMissingFieldError();
            viewDepositFunds.hideInvalidFieldError();
        }

        // --- VALIDATION with early returns ---
        if (amount == null) {
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_amount));
            return;
        }
        if (!isValidAmount(amount)) {
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_amount));
            return;
        }

        if (checkEmpty(bankName)) {
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_bank_name));
            return;
        }
        if (!isValidBankName(bankName)) {
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_bank_name));
            return;
        }

        if (checkEmpty(branchCode)) {
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_branch_code));
            return;
        }
        if (!isValidBranchCode(branchCode)) {
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_branch_code));
            return;
        }

        if (checkEmpty(cardNumber)) {
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_card_number));
            return;
        }
        if (!isValidCardNumber(cardNumber)) {
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_card_number));
            return;
        }

        if (checkEmpty(name)) {
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_name));
            return;
        }
        if (!isValidName(name)) {
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_name));
            return;
        }

        if (checkEmpty(expiryDate)) {
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_expiry_date));
            return;
        }
        if (!isValidExpiryDate(expiryDate)) {
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_expiry_date));
            return;
        }

        if (checkEmpty(cvv)) {
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_cvv));
            return;
        }
        if (!isValidCvv(cvv)) {
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_cvv));
            return;
        }

        // --- DB call on background thread ---
        depositFundsExecutor.execute(() -> {
            User current = UserSession.getUser();
            if (current == null) {
                ContextCompat.getMainExecutor(context).execute(() -> {
                    if (viewDepositFunds != null) {
                        viewDepositFunds.showInvalidFieldError(context.getString(R.string.session_expired));
                    }
                });
                return;
            }

            String sanitizedCard = cardNumber.replaceAll("[\\s-]", ""); // baNumber

            Object[] res = MySQLConnector.createDepositBankAndPendingTransaction(
                    current.getUserEmail(),
                    amount,
                    sanitizedCard,           // baNumber
                    branchCode.trim(),       // baBranchCode
                    name.trim(),             // baName
                    bankName.trim(),         // baBank
                    context
            );

            boolean success = res != null && res.length > 0 && (res[0] instanceof Boolean) && (Boolean) res[0];
            Integer txnId   = (res != null && res.length > 1 && res[1] instanceof Integer) ? (Integer) res[1] : null;
            String errCode  = (res != null && res.length > 2 && res[2] instanceof String) ? (String)  res[2] : null;

            ContextCompat.getMainExecutor(context).execute(() -> {
                if (success) {
                    this.transactionId     = (txnId != null ? txnId : 0);
                    this.transactionAmount = amount;
                    this.rawAccountNumber  = sanitizedCard;
                    this.toUserDetails = MySQLConnector.getUserDetailsByEmail(current.getUserEmail(), context);
                    DataShare.send(this);
                    context.startActivity(new Intent(context, ConfirmDepositActivity.class));
                    if (viewDepositFunds != null) viewDepositFunds.finishActivity();
                } else {
                    String msg;
                    if ("ACCOUNT_META_MISMATCH".equals(errCode)) {
                        msg = context.getString(R.string.bank_account_meta_mismatch);
                    } else {
                        msg = context.getString(R.string.deposit_failed_try_again);
                    }
                    if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(msg);
                }

            });
        });
    }


    private boolean isValidCvv(String cvv) {
        if (cvv == null) return false;
        String digits = cvv.trim();
        return digits.matches("^\\d{3,4}$");
    }



    private boolean isValidExpiryDate(String value) {
        if (value == null) return false;
        if (!value.matches("^(0[1-9]|1[0-2])/(\\d{4})$")) return false;
        String[] parts = value.split("/");
        int month = Integer.parseInt(parts[0]);
        int year  = Integer.parseInt(parts[1]);
        try {
            java.time.YearMonth entered = java.time.YearMonth.of(year, month);
            java.time.YearMonth now = java.time.YearMonth.now(java.time.ZoneId.systemDefault());

            if (entered.isBefore(now)) return false;
            return true;
        } catch (Throwable t) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int curYear = cal.get(java.util.Calendar.YEAR);
            int curMonth = cal.get(java.util.Calendar.MONTH) + 1;
            if (year < curYear) return false;
            if (year == curYear && month < curMonth) return false;
            return true;
        }
    }

    private boolean isValidName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        if (trimmed.length() < 2) return false;
        return trimmed.matches("^[A-Za-zÀ-ÖØ-öø-ÿ' -]+$");
    }


    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String sanitized = cardNumber.replaceAll("\\s+", "").replaceAll("-", "");
        return sanitized.matches("\\d{16}");
    }


    private boolean isValidBranchCode(String branchCode) {
        if (branchCode == null) return false;
        return branchCode.matches("^\\d{6}$");
    }
    
    private boolean isValidBankName(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) return false;

        for (String valid : VALID_BANKS) {
            if (valid.equalsIgnoreCase(bankName.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidAmount(Float amount) {
        if (amount == null) return false;
        if (amount <= 0f) return false;
        return true;
    }

    private boolean checkEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public void handleCancelDepositFunds() {
        DataShare.send(this);
     //   Intent intent = new Intent(context, ProfileActivity.class);
     //   context.startActivity(intent);
        if (viewDepositFunds != null) {
            viewDepositFunds.finishActivity();
        }
    }
    public void setConfirmDepositView(IConfirmDepositView view) {
        this.confirmDepositView = view;
    }
    private String maskAccount(String accDigits) {
        if (accDigits == null) return "";
        String d = accDigits.replaceAll("\\D", "");
        if (d.length() <= 4) return "**** " + d;
        String last4 = d.substring(d.length() - 4);
        return "**** **** **** " + last4;
    }
    public void handleLoadUsersUI() {
        if (confirmDepositView == null) return;
        confirmDepositView.displayAmount(transactionAmount);

        if (toUserDetails instanceof Student) {
            Student s = (Student) toUserDetails;
            confirmDepositView.displayToUserName(s.getStudentFullName());
            confirmDepositView.displayToUserNumber(s.getStudentNumber());
        } else if (toUserDetails instanceof Business) {
            Business b = (Business) toUserDetails;
            confirmDepositView.displayToUserName(b.getBusinessName());
            confirmDepositView.displayToUserNumber(b.getBusinessVAT());
        } else if (toUserDetails != null) {
            confirmDepositView.displayToUserName(toUserDetails.getUserEmail());
            confirmDepositView.displayToUserNumber("");
        }

        String masked = maskAccount(rawAccountNumber);
        confirmDepositView.displayFromUserName(masked);
        confirmDepositView.displayFromUserNumber("");
    }
    public void handleConfirmDeposit() {
        depositFundsExecutor.execute(() -> {
            try {
                MySQLConnector.updateTransactionStatus(transactionId, "success", context);
                ContextCompat.getMainExecutor(context).execute(() -> {
                    Intent intent = new Intent(context, ShowSuccessActivity.class);
                    intent.putExtra("TRANSACTION_ID", transactionId);
                    context.startActivity(intent);
                    if (confirmDepositView != null) confirmDepositView.finishActivity();
                });
            } catch (Exception e) {
                ContextCompat.getMainExecutor(context).execute(() -> {
                    Intent intent = new Intent(context, ShowFailedActivity.class);
                    intent.putExtra("TRANSACTION_ID", transactionId);
                    intent.putExtra("ERROR_REASON", "Unexpected error confirming deposit");
                    context.startActivity(intent);
                    if (confirmDepositView != null) confirmDepositView.finishActivity();
                });
            }
        });
    }
    public void handleCancelConfirmDeposit() {
        depositFundsExecutor.execute(() -> {
            try {
                MySQLConnector.updateTransactionStatus(transactionId, "failed", context);
            } catch (Exception ignored) {}
            ContextCompat.getMainExecutor(context).execute(() -> {
                DataShare.send(this);
               // context.startActivity(new Intent(context, ViewProfileActivity.class));
                viewDepositFunds.finishActivity();
            });
        });
    }

}
