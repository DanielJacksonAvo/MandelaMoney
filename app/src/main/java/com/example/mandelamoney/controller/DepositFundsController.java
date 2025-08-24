package com.example.mandelamoney.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.mandelamoney.view.activity.DepositFundsActivity;
import com.example.mandelamoney.view.activity.ShowSuccessActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DepositFundsController {
    private static final String TAG = "DepositFunds";

    private final Context context;
    private final IDepositFundsView viewDepositFunds;
    private final ExecutorService depositFundsExecutor = Executors.newSingleThreadExecutor();
    private String fromAccountName;
    private ITransactionStatusDisplayView transactionStatusDisplayView;


    private int transactionId;
    private float transactionAmount;
    private String rawAccountNumber;  // sanitized digits used to build masked view
    private User toUserDetails;       // current (logged-in) user
    private IConfirmDepositView confirmDepositView;

    private static final String[] VALID_BANKS = {
            "ABSA","Capitec","Discovery Bank","FNB","Investec",
            "Nedbank","Standard Bank","TymeBank","African Bank"
    };

    public DepositFundsController(Context context, IDepositFundsView viewDepositFunds){
        this.context = context;
        this.viewDepositFunds = viewDepositFunds;
    }

    public void handleDepositFunds(Float amount, String bankName, String branchCode, String cardNumber, String name, String expiryDate, String cvv) {
        Log.d(TAG, "handleDepositFunds() called with: " +
                "amount=" + amount +
                ", bankName=" + safe(bankName) +
                ", branchCode=" + safeBranch(branchCode) +
                ", cardNumber=" + maskCard(cardNumber) +
                ", name=" + safeName(name) +
                ", expiryDate=" + safe(expiryDate) +
                ", cvv=" + maskCvv(cvv));

        if (viewDepositFunds != null) {
            viewDepositFunds.hideMissingFieldError();
            viewDepositFunds.hideInvalidFieldError();
        }

        // --- VALIDATION with early returns ---
        if (amount == null) {
            Log.w(TAG, "Validation failed: amount is null");
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_amount));
            return;
        }
        if (!isValidAmount(amount)) {
            Log.w(TAG, "Validation failed: invalid amount " + amount);
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_amount));
            return;
        }

        if (checkEmpty(bankName)) {
            Log.w(TAG, "Validation failed: bank name empty");
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_bank_name));
            return;
        }
        if (!isValidBankName(bankName)) {
            Log.w(TAG, "Validation failed: bank not in allowlist: " + safe(bankName));
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_bank_name));
            return;
        }

        if (checkEmpty(branchCode)) {
            Log.w(TAG, "Validation failed: branchCode empty");
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_branch_code));
            return;
        }
        if (!isValidBranchCode(branchCode)) {
            Log.w(TAG, "Validation failed: branchCode invalid format: " + safeBranch(branchCode));
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_branch_code));
            return;
        }

        if (checkEmpty(cardNumber)) {
            Log.w(TAG, "Validation failed: cardNumber empty");
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_card_number));
            return;
        }
        if (!isValidCardNumber(cardNumber)) {
            Log.w(TAG, "Validation failed: cardNumber invalid: " + maskCard(cardNumber));
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_card_number));
            return;
        }

        if (checkEmpty(name)) {
            Log.w(TAG, "Validation failed: name empty");
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_name));
            return;
        }
        if (!isValidName(name)) {
            Log.w(TAG, "Validation failed: name invalid: " + safeName(name));
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_name));
            return;
        }

        if (checkEmpty(expiryDate)) {
            Log.w(TAG, "Validation failed: expiryDate empty");
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_expiry_date));
            return;
        }
        if (!isValidExpiryDate(expiryDate)) {
            Log.w(TAG, "Validation failed: expiryDate invalid: " + safe(expiryDate));
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_expiry_date));
            return;
        }

        if (checkEmpty(cvv)) {
            Log.w(TAG, "Validation failed: cvv empty");
            if (viewDepositFunds != null) viewDepositFunds.showMissingFieldError(context.getString(R.string.enter_cvv));
            return;
        }
        if (!isValidCvv(cvv)) {
            Log.w(TAG, "Validation failed: cvv invalid length");
            if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.invalid_cvv));
            return;
        }

        // --- DB call on background thread ---
        depositFundsExecutor.execute(() -> {
            try {
                User current = UserSession.getUser();
                Log.d(TAG, "BG: fetched current user = " + (current != null ? safe(current.getUserEmail()) : "null"));

                if (current == null) {
                    ContextCompat.getMainExecutor(context).execute(() -> {
                        Log.w(TAG, "BG->UI: session expired, notifying view");
                        if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(context.getString(R.string.session_expired));
                    });
                    return;
                }

                String sanitizedCard = cardNumber.replaceAll("[\\s-]", "");
                Log.d(TAG, "BG: sanitized card ready (masked)=" + maskCard(sanitizedCard));

                Log.i(TAG, "BG: calling MySQLConnector.createDepositBankAndPendingTransaction...");
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

                Log.i(TAG, "BG: DB returned -> success=" + success + ", txnId=" + txnId + ", errCode=" + errCode);

                ContextCompat.getMainExecutor(context).execute(() -> {
                    if (success) {
                        // inside the success branch of handleDepositFunds(...)
                        this.transactionId     = (txnId != null ? txnId : 0);
                        this.transactionAmount = amount;
                        this.rawAccountNumber  = sanitizedCard;
                        this.fromAccountName   = name.trim();   // <-- NEW: keep the display name
                        this.toUserDetails     = MySQLConnector.getUserDetailsByEmail(current.getUserEmail(), context);

                        Log.d(TAG, "UI: Data ready, sending via DataShare and launching ConfirmDepositActivity. "
                                + "transactionId=" + this.transactionId
                                + ", amount=" + this.transactionAmount
                                + ", toUser=" + (toUserDetails != null ? toUserDetails.getUserEmail() : "null"));

                        DataShare.send(this);

                        Intent intent = new Intent(context, ConfirmDepositActivity.class);
                        maybeAddNewTaskFlag(intent);
                        try {
                            context.startActivity(intent);
                            Log.i(TAG, "UI: startActivity(ConfirmDepositActivity) called");
                        } catch (Exception startEx) {
                            Log.e(TAG, "UI: Failed to start ConfirmDepositActivity", startEx);
                            Toast.makeText(context, "Unable to open confirmation screen.", Toast.LENGTH_SHORT).show();
                        }

                        if (viewDepositFunds != null) {
                            viewDepositFunds.finishActivity();
                            Log.d(TAG, "UI: viewDepositFunds.finishActivity() requested");
                        }
                    } else {
                        String msg;
                        if ("ACCOUNT_META_MISMATCH".equals(errCode)) {
                            msg = context.getString(R.string.bank_account_meta_mismatch);
                        } else {
                            msg = context.getString(R.string.deposit_failed_try_again);
                        }
                        Log.w(TAG, "UI: deposit failed; errCode=" + errCode + ", showing message");
                        if (viewDepositFunds != null) viewDepositFunds.showInvalidFieldError(msg);
                    }
                });
            } catch (Throwable t) {
                Log.e(TAG, "BG: Unexpected error during deposit flow", t);
                ContextCompat.getMainExecutor(context).execute(() ->
                        Toast.makeText(context, "Unexpected error. Please try again.", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public void handleCancelDepositFunds() {
        Log.d(TAG, "handleCancelDepositFunds()");
        DataShare.send(this);
        if (viewDepositFunds != null) {
            viewDepositFunds.finishActivity();
            Log.d(TAG, "finishActivity() from handleCancelDepositFunds");
        }
    }

    public void setConfirmDepositView(IConfirmDepositView view) {
        this.confirmDepositView = view;
        Log.d(TAG, "setConfirmDepositView() set? " + (view != null));
    }

    public void handleLoadUsersUI() {
        Log.d(TAG, "handleLoadUsersUI() amount=" + transactionAmount
                + ", toUser=" + (toUserDetails != null ? toUserDetails.getUserEmail() : "null")
                + ", maskedAccount=" + maskAccount(rawAccountNumber)
                + ", fromAccountName=" + fromAccountName);

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

        // ----- THIS is the important bit -----
        String masked = maskAccount(rawAccountNumber);
        String displayName = (fromAccountName != null && !fromAccountName.trim().isEmpty())
                ? fromAccountName.trim()
                : context.getString(R.string.cardholder_name_fallback);

        confirmDepositView.displayFromUserName(displayName);
        confirmDepositView.displayFromUserNumber(masked);
    }

    public void handleConfirmDeposit() {
        Log.i(TAG, "handleConfirmDeposit() txnId=" + transactionId);
        depositFundsExecutor.execute(() -> {
            try {
                // 1) DB: mark success (this also stamps date/time and credits balance per proc above)
                MySQLConnector.updateTransactionStatus(transactionId, "success", context);
                Log.i(TAG, "BG: updateTransactionStatus -> success");

                // 2) App: refresh session balance so UI shows the new total
                Executors.newSingleThreadExecutor().execute(() -> {
                    float updated = UserSession.updateBalance(context);
                    User u = UserSession.getUser();
                    if (u != null) u.setUserBalance(updated);
                    Log.d(TAG, "Session balance refreshed to: " + updated);
                });

                // 3) Go to success screen (and pass this controller for display)
                ContextCompat.getMainExecutor(context).execute(() -> {
                    // make sure ShowSuccessActivity can render deposit details
                    DataShare.send(this);

                    Intent intent = new Intent(context, ShowSuccessActivity.class);
                    intent.putExtra("TRANSACTION_ID", transactionId);
                    maybeAddNewTaskFlag(intent);
                    try {
                        context.startActivity(intent);
                        Log.i(TAG, "UI: startActivity(ShowSuccessActivity) called");
                    } catch (Exception startEx) {
                        Log.e(TAG, "UI: Failed to start ShowSuccessActivity", startEx);
                        Toast.makeText(context, "Could not open success screen.", Toast.LENGTH_SHORT).show();
                    }

                    if (confirmDepositView != null) confirmDepositView.finishActivity();
                });

            } catch (Exception e) {
                Log.e(TAG, "BG: updateTransactionStatus threw, showing failure screen", e);
                ContextCompat.getMainExecutor(context).execute(() -> {
                    Intent intent = new Intent(context, ShowFailedActivity.class);
                    intent.putExtra("TRANSACTION_ID", transactionId);
                    intent.putExtra("ERROR_REASON", "Unexpected error confirming deposit");
                    maybeAddNewTaskFlag(intent);
                    try {
                        context.startActivity(intent);
                        Log.i(TAG, "UI: startActivity(ShowFailedActivity) called");
                    } catch (Exception startEx) {
                        Log.e(TAG, "UI: Failed to start ShowFailedActivity", startEx);
                        Toast.makeText(context, "Could not open failure screen.", Toast.LENGTH_SHORT).show();
                    }
                    if (confirmDepositView != null) confirmDepositView.finishActivity();
                });
            }
        });
    }


    public void handleCancelConfirmDeposit() {
        Log.i(TAG, "handleCancelConfirmDeposit() txnId=" + transactionId);
        depositFundsExecutor.execute(() -> {
            try {
                MySQLConnector.updateTransactionStatus(transactionId, "failed", context);
                Log.i(TAG, "BG: updateTransactionStatus -> failed");
            } catch (Exception ignored) {
                Log.w(TAG, "BG: updateTransactionStatus failed silently in cancel");
            }
            ContextCompat.getMainExecutor(context).execute(() -> {
                Intent intent = new Intent(context, DepositFundsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                maybeAddNewTaskFlag(intent);
                try {
                    context.startActivity(intent);
                    Log.i(TAG, "UI: startActivity(DepositFundsActivity) called");
                } catch (Exception startEx) {
                    Log.e(TAG, "UI: Failed to start DepositFundsActivity", startEx);
                    Toast.makeText(context, "Could not return to deposit screen.", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "UI: finishing confirm screen on cancel");
                if (confirmDepositView != null) confirmDepositView.finishActivity();
            });
        });
    }

    private static String maskCard(String cardNumber) {
        if (cardNumber == null) return "(null)";
        String d = cardNumber.replaceAll("\\D", "");
        if (d.length() <= 4) return "**** " + d;
        String last4 = d.substring(d.length() - 4);
        return "**** **** **** " + last4;
    }

    private static String maskCvv(String cvv) {
        if (cvv == null || cvv.isEmpty()) return "(empty)";
        return "***";
    }

    private static String safe(String s) { return s == null ? "(null)" : s.trim(); }

    private static String safeName(String s) {
        if (s == null) return "(null)";
        String t = s.trim();
        return t.isEmpty() ? "(empty)" : t;
    }

    private static String safeBranch(String s) {
        if (s == null) return "(null)";
        String t = s.trim();
        return t.matches("\\d{6}") ? t.substring(0,3) + "***" : "(invalid)";
    }

    private void maybeAddNewTaskFlag(Intent intent) {
        // If context is NOT an Activity, we must add NEW_TASK to avoid ActivityNotFound/BadToken edge cases
        if (!(context instanceof Activity)) {
            Log.w(TAG, "Context is not an Activity; adding FLAG_ACTIVITY_NEW_TASK");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    // ----------------- validators -----------------

    private boolean isValidCvv(String cvv) {
        boolean ok = cvv != null && cvv.trim().matches("^\\d{3,4}$");
        Log.d(TAG, "validate cvv -> " + ok);
        return ok;
    }

    private boolean isValidExpiryDate(String value) {
        if (value == null) { Log.d(TAG, "validate expiry -> false (null)"); return false; }
        if (!value.matches("^(0[1-9]|1[0-2])/(\\d{4})$")) { Log.d(TAG, "validate expiry -> false (format)"); return false; }
        String[] parts = value.split("/");
        int month = Integer.parseInt(parts[0]);
        int year  = Integer.parseInt(parts[1]);
        try {
            java.time.YearMonth entered = java.time.YearMonth.of(year, month);
            java.time.YearMonth now = java.time.YearMonth.now(java.time.ZoneId.systemDefault());
            boolean ok = !entered.isBefore(now);
            Log.d(TAG, "validate expiry -> " + ok + " (entered=" + entered + ", now=" + now + ")");
            return ok;
        } catch (Throwable t) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int curYear = cal.get(java.util.Calendar.YEAR);
            int curMonth = cal.get(java.util.Calendar.MONTH) + 1;
            boolean ok = !(year < curYear || (year == curYear && month < curMonth));
            Log.d(TAG, "validate expiry (fallback) -> " + ok + " (y=" + year + ", m=" + month + ")");
            return ok;
        }
    }

    private boolean isValidName(String name) {
        boolean ok = name != null && name.trim().length() >= 2 &&
                name.trim().matches("^[A-Za-zÀ-ÖØ-öø-ÿ' -]+$");
        Log.d(TAG, "validate name -> " + ok);
        return ok;
    }

    private boolean isValidCardNumber(String cardNumber) {
        boolean ok = cardNumber != null &&
                cardNumber.replaceAll("\\s+", "").replaceAll("-", "").matches("\\d{16}");
        Log.d(TAG, "validate card -> " + ok);
        return ok;
    }

    private boolean isValidBranchCode(String branchCode) {
        boolean ok = branchCode != null && branchCode.matches("^\\d{6}$");
        Log.d(TAG, "validate branch -> " + ok);
        return ok;
    }

    private boolean isValidBankName(String bankName) {
        boolean ok = bankName != null && !bankName.trim().isEmpty();
        if (!ok) { Log.d(TAG, "validate bank -> false (empty)"); return false; }
        for (String valid : VALID_BANKS) {
            if (valid.equalsIgnoreCase(bankName.trim())) {
                Log.d(TAG, "validate bank -> true (" + bankName + ")");
                return true;
            }
        }
        Log.d(TAG, "validate bank -> false (not allowlisted): " + bankName);
        return false;
    }

    private boolean isValidAmount(Float amount) {
        boolean ok = amount != null && amount > 0f;
        Log.d(TAG, "validate amount -> " + ok);
        return ok;
    }

    private boolean checkEmpty(String s) {
        boolean empty = (s == null || s.isEmpty());
        if (empty) Log.d(TAG, "checkEmpty -> true");
        return empty;
    }
    private static String maskAccount(String value) {
        if (value == null) return "(null)";
        String d = value.replaceAll("\\D", ""); // keep digits only
        if (d.length() <= 4) return "**** " + d;
        return "**** **** **** " + d.substring(d.length() - 4);
    }
    public void setTransactionStatusDisplayView(ITransactionStatusDisplayView transactionStatusDisplayView){
        this.transactionStatusDisplayView = transactionStatusDisplayView;
    }
    public void loadTransactionStatusDataForSuccess(){
        if(transactionStatusDisplayView == null)return;
        transactionStatusDisplayView.displayAmount(transactionAmount);
        if (toUserDetails instanceof Student) {
            Student s = (Student) toUserDetails;
            transactionStatusDisplayView.displayToUserName(s.getStudentFullName());
            transactionStatusDisplayView.displayToUserNumber(s.getStudentNumber());
        } else if (toUserDetails instanceof Business) {
            Business b = (Business) toUserDetails;
            transactionStatusDisplayView.displayToUserName(b.getBusinessName());
            transactionStatusDisplayView.displayToUserNumber(b.getBusinessVAT());
        } else if (toUserDetails != null) {
            transactionStatusDisplayView.displayToUserName(toUserDetails.getUserEmail());
            transactionStatusDisplayView.displayToUserNumber("");
        }

        // "From" = cardholder name + masked account
        String displayName = (fromAccountName != null && !fromAccountName.trim().isEmpty())
                ? fromAccountName.trim()
                : "Cardholder";
        transactionStatusDisplayView.displayFromUserName(displayName);
        transactionStatusDisplayView.displayFromUserNumber(maskAccount(rawAccountNumber));
    }

}
