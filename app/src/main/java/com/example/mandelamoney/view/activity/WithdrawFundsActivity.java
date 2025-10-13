package com.example.mandelamoney.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.WithdrawFundsController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IWithdrawFundsView;

public class WithdrawFundsActivity extends AppCompatActivity implements IWithdrawFundsView {
    private WithdrawFundsController controller;
    TextView txtWithdrawFundsError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Object payload = DataShare.receive();
        if (UserSession.getUser() == null && payload instanceof com.example.mandelamoney.model.User) {
            UserSession.setUser((com.example.mandelamoney.model.User) payload);
            Log.d("WithdrawFundsActivity", "Restored user from DataShare.");
        }

        if (UserSession.getUser() == null) {
            Log.w("WithdrawFundsActivity", "No session; routing to LoginActivity");
            startActivity(new Intent(this, com.example.mandelamoney.view.activity.LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_withdraw_funds);

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        setController();
        connectToUI();
    }
    private void setController() {
        controller = new com.example.mandelamoney.controller.WithdrawFundsController(this, this);
    }
    private void connectToUI() {
        TextView btnCancel = findViewById(R.id.btn_cancel_withdraw_funds);
        EditText tbxAmount = findViewById(R.id.tbx_amount_withdraw_funds);
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_withdraw_funds);
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_withdraw_funds);
        EditText tbxCardNumber = findViewById(R.id.tbx_card_number_withdraw_funds);
        EditText tbxName = findViewById(R.id.tbx_name_withdraw_funds);
        Button btnWithdrawFunds = findViewById(R.id.btn_withdraw_funds);
        txtWithdrawFundsError = findViewById(R.id.txt_error_withdraw_funds);
        configureCancelButton(btnCancel);
        configureWithdrawFundsButton(btnWithdrawFunds, tbxAmount, tbxBankName, tbxBranchCode, tbxCardNumber, tbxName);
    }
    private void configureWithdrawFundsButton(
            Button btnWithdrawFunds,
            EditText tbxAmount,
            EditText tbxBankName,
            EditText tbxBranchCode,
            EditText tbxCardNumber,
            EditText tbxName
    ) {
        btnWithdrawFunds.setOnClickListener(v -> {
            Float amount = null;
            String amtTxt = tbxAmount.getText().toString().trim();
            if (!amtTxt.isEmpty()) {
                try { amount = Float.parseFloat(amtTxt); } catch (NumberFormatException ignored) { /* keep null */ }
            }

            controller.handleWithdrawFunds(
                    amount,
                    tbxBankName.getText().toString(),
                    tbxBranchCode.getText().toString(),
                    tbxCardNumber.getText().toString(),
                    tbxName.getText().toString()
            );
        });
    }
    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCancelWithdrawFunds());
    }
    @Override
    public void showMissingFieldError(String message) {
        txtWithdrawFundsError.setVisibility(View.VISIBLE);
        txtWithdrawFundsError.setText(message);

    }

    @Override
    public void hideMissingFieldError() {
        txtWithdrawFundsError.setVisibility(View.GONE);
    }

    @Override
    public void showInvalidFieldError(String message) {
        txtWithdrawFundsError.setVisibility(View.VISIBLE);
        txtWithdrawFundsError.setText(message);
    }

    @Override
    public void hideInvalidFieldError() {
        txtWithdrawFundsError.setVisibility(View.GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }
    private static class DateSlashTextWatcher implements TextWatcher {
        private final EditText editText;
        private boolean selfChange;

        DateSlashTextWatcher(EditText et) {
            this.editText = et;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (selfChange) return;

            String raw = s.toString();
            int cursor = editText.getSelectionStart();

            String digits = raw.replaceAll("\\D", "");
            if (digits.length() > 6) digits = digits.substring(0, 6);

            String formatted;
            boolean rawHadSlash = raw.contains("/");
            boolean insertingSlashNow = false;

            if (digits.length() < 2) {
                formatted = digits;
            } else if (digits.length() == 2) {
                formatted = digits + "/";
                insertingSlashNow = !rawHadSlash;
            } else {
                formatted = digits.substring(0, 2) + "/" + digits.substring(2);
                insertingSlashNow = !rawHadSlash;
            }

            if (!formatted.equals(raw)) {
                selfChange = true;
                editText.setText(formatted);

                int newPos;
                if (digits.length() == 2 && !rawHadSlash) {
                    newPos = 3;
                } else if (insertingSlashNow && cursor >= 2) {
                    newPos = Math.min(formatted.length(), cursor + 1);
                } else {
                    newPos = Math.min(formatted.length(), Math.max(0, cursor));
                }

                editText.setSelection(newPos);
                selfChange = false;
            }
        }
    }

}
