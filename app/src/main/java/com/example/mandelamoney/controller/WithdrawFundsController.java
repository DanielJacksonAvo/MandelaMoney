package com.example.mandelamoney.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IConfirmWithdrawView;
import com.example.mandelamoney.view.Iface.ITransactionStatusDisplayView;
import com.example.mandelamoney.view.Iface.IWithdrawFundsView;
import com.example.mandelamoney.view.activity.ConfirmWithdrawActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;
import com.example.mandelamoney.view.activity.ShowSuccessActivity;
import com.example.mandelamoney.view.activity.WithdrawFundsActivity;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WithdrawFundsController {
    private static final String TAG = "WithdrawFunds";
    private final Context context;
    private final IWithdrawFundsView viewWithdrawFunds;
    private final ExecutorService withdrawFundsExecutor = Executors.newSingleThreadExecutor();
    private String toAccountName;
    private ITransactionStatusDisplayView transactionStatusDisplayView;

    private int transactionId;
    private float transactionAmount;
    private String rawAccountNumber;
    private User fromUserDetails;
    private IConfirmWithdrawView confirmWithdrawView;
   private static final String[] VALID_BANKS = {
           "ABSA","Capitec","Discovery Bank","FNB","Investec",
           "Nedbank","Standard Bank","TymeBank","African Bank"
   };
   public WithdrawFundsController(Context context, IWithdrawFundsView viewWithdrawFunds){
       this.context = context;
       this.viewWithdrawFunds = viewWithdrawFunds;
   }
   public void handleWithdrawFunds(Float amount, String bankName, String branchCode, String cardNumber, String name){
       if(viewWithdrawFunds != null){
           viewWithdrawFunds.hideMissingFieldError();
           viewWithdrawFunds.hideInvalidFieldError();
       }
       if(amount == null){
           Log.w(TAG, "Validation failed: amount is null");
           if (viewWithdrawFunds != null) viewWithdrawFunds.showMissingFieldError(context.getString(R.string.enter_amount));
           return;
       }
       if (!isValidAmount(amount)) {
           Log.w(TAG, "Validation failed: invalid amount " + amount);
           if (viewWithdrawFunds != null) viewWithdrawFunds.showInvalidFieldError(context.getString(R.string.invalid_amount));
           return;
       }
       if (checkEmpty(bankName)) {
           Log.w(TAG, "Validation failed: bank name empty");
           if (viewWithdrawFunds != null) viewWithdrawFunds.showMissingFieldError(context.getString(R.string.enter_bank_name));
           return;
       }
       if (!isValidBankName(bankName)) {
           Log.w(TAG, "Validation failed: bank not in allowlist: " + safe(bankName));
           if (viewWithdrawFunds != null) viewWithdrawFunds.showInvalidFieldError(context.getString(R.string.invalid_bank_name));
           return;
       }

       if (checkEmpty(branchCode)) {
           Log.w(TAG, "Validation failed: branchCode empty");
           if (viewWithdrawFunds != null) viewWithdrawFunds.showMissingFieldError(context.getString(R.string.enter_branch_code));
           return;
       }
       if (!isValidBranchCode(branchCode)) {
           Log.w(TAG, "Validation failed: branchCode invalid format: " + safeBranch(branchCode));
           if (viewWithdrawFunds != null) viewWithdrawFunds.showInvalidFieldError(context.getString(R.string.invalid_branch_code));
           return;
       }

       if (checkEmpty(cardNumber)) {
           Log.w(TAG, "Validation failed: cardNumber empty");
           if (viewWithdrawFunds != null) viewWithdrawFunds.showMissingFieldError(context.getString(R.string.enter_card_number));
           return;
       }
       if (!isValidCardNumber(cardNumber)) {
           Log.w(TAG, "Validation failed: cardNumber invalid: " + maskCard(cardNumber));
           if (viewWithdrawFunds != null) viewWithdrawFunds.showInvalidFieldError(context.getString(R.string.invalid_card_number));
           return;
       }

       if (checkEmpty(name)) {
           Log.w(TAG, "Validation failed: name empty");
           if (viewWithdrawFunds != null) viewWithdrawFunds.showMissingFieldError(context.getString(R.string.enter_name));
           return;
       }
       if (!isValidName(name)) {
           Log.w(TAG, "Validation failed: name invalid: " + safeName(name));
           if (viewWithdrawFunds != null) viewWithdrawFunds.showInvalidFieldError(context.getString(R.string.invalid_name));
           return;
       }
       withdrawFundsExecutor.execute(() -> {
           try {
               User current = UserSession.getUser();
               Log.d(TAG, "BG: fetched current user = " + (current != null ? safe(current.getUserEmail()) : "null"));

               if(current == null){
                   ContextCompat.getMainExecutor(context).execute(() -> {
                       Log.w(TAG, "BG->UI: session expired, notifying view");
                       if (viewWithdrawFunds != null)
                           viewWithdrawFunds.showInvalidFieldError(context.getString(R.string.session_expired));
                   });
                   return;
               }
               String sanitizedCard = cardNumber.replaceAll("[\\s-]", "");
               Log.d(TAG, "BG: sanitized card ready (masked)=" + maskCard(sanitizedCard));

               Log.i(TAG, "BG: calling MySQLConnector.createWithdrawBankAndPendingTransaction...");
               Object[] res = MySQLConnector.createWithdrawBankAndPendingTransaction(
                       current.getUserEmail(),
                       amount,
                       sanitizedCard,
                       branchCode.trim(),
                       name.trim(),
                       bankName.trim(),
                       context
               );
               boolean success = res != null && res.length > 0 && (res[0] instanceof Boolean) && (Boolean) res[0];
               Integer txnId   = (res != null && res.length > 1 && res[1] instanceof Integer) ? (Integer) res[1] : null;
               String errCode  = (res != null && res.length > 2 && res[2] instanceof String) ? (String)  res[2] : null;

               Log.i(TAG, "BG: DB returned -> success=" + success + ", txnId=" + txnId + ", errCode=" + errCode);

               ContextCompat.getMainExecutor(context).execute(() -> {
                   if (success) {
                       this.transactionId     = (txnId != null ? txnId : 0);
                       this.transactionAmount = amount;
                       this.rawAccountNumber  = sanitizedCard;
                       this.toAccountName     = name.trim();
                       this.fromUserDetails   = MySQLConnector.getUserDetailsByEmail(current.getUserEmail(), context);

                       Log.d(TAG, "UI: Data ready, launching ConfirmWithdrawActivity. txnId=" + this.transactionId);

                       DataShare.send(this);
                       Intent intent = new Intent(context, ConfirmWithdrawActivity.class);
                       maybeAddNewTaskFlag(intent);
                       try {
                           context.startActivity(intent);
                           Log.i(TAG, "UI: startActivity(ConfirmWithdrawActivity) called");
                       } catch (Exception startEx) {
                           Log.e(TAG, "UI: Failed to start ConfirmWithdrawActivity", startEx);
                           Toast.makeText(context, "Unable to open confirmation screen.", Toast.LENGTH_SHORT).show();
                       }

                       if (viewWithdrawFunds != null) {
                           viewWithdrawFunds.finishActivity();
                           Log.d(TAG, "UI: viewWithdrawFunds.finishActivity() requested");
                       }
                   } else {
                       String msg;
                       if ("ACCOUNT_META_MISMATCH".equals(errCode)) {
                           msg = context.getString(R.string.bank_account_meta_mismatch);
                       } else if ("INSUFFICIENT_FUNDS".equals(errCode)) {
                           msg = context.getString(R.string.insufficient_funds);
                       } else if ("INVALID_BRANCH".equals(errCode)) {
                           msg = context.getString(R.string.invalid_branch_code);
                       } else {
                           msg = context.getString(R.string.withdraw_failed_try_again);
                       }

                       Log.w(TAG, "UI: withdraw failed; errCode=" + errCode + ", txnId=" + txnId + " (showing message)");
                       if (viewWithdrawFunds != null) {
                           viewWithdrawFunds.showInvalidFieldError(msg);
                       } else {
                           Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                       }
                   }
               });



           }catch (Throwable t){
               Log.e(TAG, "BG: Unexpected error during withdraw flow", t);
               ContextCompat.getMainExecutor(context).execute(() ->
                       Toast.makeText(context, "Unexpected error. Please try again.", Toast.LENGTH_SHORT).show()
               );
           }
       });
   }
   public void handleCancelWithdrawFunds(){
       Log.d(TAG, "handleCancelWithdrawFunds()");
       DataShare.send(this);
       if(viewWithdrawFunds != null){
           viewWithdrawFunds.finishActivity();
           Log.d(TAG, "finishActivity() from handleCancelWithdrawFunds");
       }
   }
   public void setConfirmWithdrawView(ConfirmWithdrawActivity view){
       this.confirmWithdrawView = view;
       Log.d(TAG, "setConfirmWithdrawView() set? " + (view != null));
   }
   public void handleLoadUsersUI(){
       Log.d(TAG, "handleLoadUsersUI() amount=" + transactionAmount
               + ", fromUser=" + (fromUserDetails != null ? fromUserDetails.getUserEmail() : "null")
               + ", maskedAccount=" + maskAccount(rawAccountNumber)
               + ", toAccountName=" + toAccountName);
       if(confirmWithdrawView == null) return;
       confirmWithdrawView.displayAmount(transactionAmount);
       if(fromUserDetails instanceof Student) {
           Student s = (Student) fromUserDetails;
           confirmWithdrawView.displayFromUserName(s.getStudentFullName());
           confirmWithdrawView.displayFromUserNumber(s.getStudentNumber());
       }else if(fromUserDetails instanceof Business){
           Business b = (Business) fromUserDetails;
           confirmWithdrawView.displayFromUserName(b.getBusinessName());
           confirmWithdrawView.displayFromUserNumber(b.getBusinessVAT());
       }else if(fromUserDetails != null){
           confirmWithdrawView.displayFromUserName(fromUserDetails.getUserEmail());
           confirmWithdrawView.displayFromUserNumber("");
       }
       String masked = maskAccount(rawAccountNumber);
       String displayName = (toAccountName != null && !toAccountName.trim().isEmpty())
               ? toAccountName.trim()
               : context.getString(R.string.cardholder_name_fallback);
       confirmWithdrawView.displayToUserName(displayName);
       confirmWithdrawView.displayToUserNumber(masked);
   }
   public void handleConfirmWithdraw(){
       Log.i(TAG, "handleConfirmWithdraw() txnId=" + transactionId);

       withdrawFundsExecutor.execute(()->{
           try{
               MySQLConnector.updateTransactionStatus(transactionId, "success", context);
               Log.i(TAG, "BG: updateTransactionStatus -> success");

               Executors.newSingleThreadExecutor().execute(()->{
                   float updated = UserSession.updateBalance(context);
                   User u = UserSession.getUser();
                   if(u != null) u.setUserBalance(updated);
                   Log.d(TAG, "Session balance refreshed to: " + updated);
               });
               ContextCompat.getMainExecutor(context).execute(()->{
                   DataShare.send(this);
                   Intent intent = new Intent(context, ShowSuccessActivity.class);
                   intent.putExtra("TRANSACTION_ID", transactionId);
                   maybeAddNewTaskFlag(intent);
                   try{
                       context.startActivity(intent);
                       Log.i(TAG, "UI: startActivity(ShowSuccessActivity) called");
                   }catch (Exception startEx){
                       Log.e(TAG, "UI: Failed to start ShowSuccessActivity", startEx);
                       Toast.makeText(context, "Could not open success screen.", Toast.LENGTH_SHORT).show();
                   }
                   if(confirmWithdrawView != null) confirmWithdrawView.finishActivity();

               });
           }catch(Exception e){
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
                   if (confirmWithdrawView != null) confirmWithdrawView.finishActivity();
               });
           }
       });
   }
   public void handleCancelConfirmWithdraw(){
       Log.i(TAG, "handleCancelConfirmWithdraw() txnId=" + transactionId);
       withdrawFundsExecutor.execute(() -> {
           try {
               MySQLConnector.updateTransactionStatus(transactionId, "failed", context);
               Log.i(TAG, "BG: updateTransactionStatus -> failed");
           } catch (Exception ignored) {
               Log.w(TAG, "BG: updateTransactionStatus failed silently in cancel");
           }
           ContextCompat.getMainExecutor(context).execute(() -> {
               Intent intent = new Intent(context, WithdrawFundsActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
               maybeAddNewTaskFlag(intent);
               try {
                   context.startActivity(intent);
                   Log.i(TAG, "UI: startActivity(WithdrawFundsActivity) called");
               } catch (Exception startEx) {
                   Log.e(TAG, "UI: Failed to start WithdrawFundsActivity", startEx);
                   Toast.makeText(context, "Could not return to withdraw screen.", Toast.LENGTH_SHORT).show();
               }
               Log.d(TAG, "UI: finishing confirm screen on cancel");
               if (confirmWithdrawView != null) confirmWithdrawView.finishActivity();
           });
       });
   }
    private static String maskCard(String cardNumber) {
        if (cardNumber == null) return "(null)";
        String d = cardNumber.replaceAll("\\D", "");
        int n = d.length();
        if (n == 0) return "";
        if (n <= 4) return groupBy4(d);

        String last4 = d.substring(n - 4);
        char[] a=new char[n-4];
        Arrays.fill(a,'*');
        String maskedPrefix=new String(a);

        return groupBy4(maskedPrefix + last4);
    }
    private static String groupBy4(String s) {
        StringBuilder out = new StringBuilder(s.length() + s.length() / 4);
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && i % 4 == 0) out.append(' ');
            out.append(s.charAt(i));
        }
        return out.toString();
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
        if (!(context instanceof Activity)) {
            Log.w(TAG, "Context is not an Activity; adding FLAG_ACTIVITY_NEW_TASK");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                cardNumber.replaceAll("\\s+", "").replaceAll("-", "").matches("\\d{6,}");
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
        if (fromUserDetails instanceof Student) {
            Student s = (Student) fromUserDetails;
            transactionStatusDisplayView.displayFromUserName(s.getStudentFullName());
            transactionStatusDisplayView.displayFromUserNumber(s.getStudentNumber());
        } else if (fromUserDetails instanceof Business) {
            Business b = (Business) fromUserDetails;
            transactionStatusDisplayView.displayFromUserName(b.getBusinessName());
            transactionStatusDisplayView.displayFromUserNumber(b.getBusinessVAT());
        } else if (fromUserDetails != null) {
            transactionStatusDisplayView.displayFromUserName(fromUserDetails.getUserEmail());
            transactionStatusDisplayView.displayFromUserNumber("");
        }

        String displayName = (toAccountName != null && !toAccountName.trim().isEmpty())
                ? toAccountName.trim()
                : "Cardholder";
        transactionStatusDisplayView.displayToUserName(displayName);
        transactionStatusDisplayView.displayToUserNumber(maskAccount(rawAccountNumber));
    }



}
