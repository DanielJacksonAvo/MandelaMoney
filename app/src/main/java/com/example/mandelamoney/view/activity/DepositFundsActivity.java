package com.example.mandelamoney.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DepositFundsController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IDepositFundsView;
import android.text.Editable;
import android.text.TextWatcher;

public class DepositFundsActivity extends AppCompatActivity implements IDepositFundsView {
    private DepositFundsController controller;
    TextView txtDepositAmountError;
    TextView txtDepositBankNameError;
    TextView txtDepositBranchCodeError;
    TextView txtDepositAccountNumberError;
    TextView txtDepositAccountHolderError;
    TextView txtDepositCvvError;
    TextView txtDepositExpiryDateError;
    private ConstraintLayout loadingSpinner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Object payload = DataShare.receive();
        if (UserSession.getUser() == null && payload instanceof com.example.mandelamoney.model.User) {
            UserSession.setUser((com.example.mandelamoney.model.User) payload);
            Log.d("DepositFundsActivity", "Restored user from DataShare.");
        }

        if (UserSession.getUser() == null) {
            Log.w("DepositFundsActivity", "No session; routing to LoginActivity");
            startActivity(new Intent(this, com.example.mandelamoney.view.activity.LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deposit_funds);

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        setController();
        connectToUI();
    }

    private void setController() {
        controller = new com.example.mandelamoney.controller.DepositFundsController(this, this);
    }

    private void connectToUI() {
        TextView btnCancel = findViewById(R.id.btn_cancel_deposit_funds);
        EditText tbxAmount = findViewById(R.id.tbx_amount_deposit_funds);
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_deposit_funds);
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_deposit_funds);
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_deposit_funds);
        EditText tbxAccountHolder = findViewById(R.id.tbx_name_deposit_funds);
        EditText tbxCvv = findViewById(R.id.tbx_cvv_deposit_funds);
        EditText tbxExpiryDate = findViewById(R.id.tbx_expiry_date_deposit_funds);
        Button btnDepositFunds = findViewById(R.id.btn_deposit_funds);
        txtDepositAmountError = findViewById(R.id.txt_error_amount_deposit_funds);
        txtDepositBankNameError = findViewById(R.id.txt_error_bank_name_deposit_funds);
        txtDepositBranchCodeError = findViewById(R.id.txt_error_branch_code_deposit_funds);
        txtDepositAccountNumberError = findViewById(R.id.txt_error_account_number_deposit_funds);
        txtDepositAccountHolderError = findViewById(R.id.txt_error_account_holder_deposit_funds);
        txtDepositCvvError = findViewById(R.id.txt_error_cvv_deposit_funds);
        txtDepositExpiryDateError = findViewById(R.id.txt_error_expiry_date_holder_deposit_funds);
        tbxExpiryDate.addTextChangedListener(new DateSlashTextWatcher(tbxExpiryDate));
        configureCancelButton(btnCancel);
        configureDepositFundsButton(btnDepositFunds, tbxAmount, tbxBankName, tbxBranchCode, tbxAccountNumber, tbxAccountHolder,tbxCvv,tbxExpiryDate);
        loadingSpinner = findViewById(R.id.deposit_funds_loading_spinner);
    }

    private void configureDepositFundsButton(
            Button btnDepositFunds,
            EditText tbxAmount,
            EditText tbxBankName,
            EditText tbxBranchCode,
            EditText tbxCardNumber,
            EditText tbxName,
            EditText tbxCvv,
            EditText tbxExpiryDate
    ) {
        btnDepositFunds.setOnClickListener(v -> controller.handleDepositFunds(
                tbxAmount.getText().toString().trim(),
                tbxBankName.getText().toString(),
                tbxBranchCode.getText().toString(),
                tbxCardNumber.getText().toString(),
                tbxName.getText().toString(),
                tbxCvv.getText().toString(),
                tbxExpiryDate.getText().toString()
        ));
    }


    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCancelDepositFunds());
    }


    @Override
    public void showMissingAmountError(String message) {
        EditText tbxAmount = findViewById(R.id.tbx_amount_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAmount);
        txtDepositAmountError.setVisibility(View.VISIBLE);
        txtDepositAmountError.setText(message);

    }

    @Override
    public void hideMissingAmountError() {
        EditText tbxAmount = findViewById(R.id.tbx_amount_deposit_funds);
        ErrorBorder.removeStroke(tbxAmount);
        txtDepositAmountError.setVisibility(View.GONE);

    }

    @Override
    public void showInvalidAmountError(String message) {
        EditText tbxAmount = findViewById(R.id.tbx_amount_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAmount);
        txtDepositAmountError.setVisibility(View.VISIBLE);
        txtDepositAmountError.setText(message);

    }
    @Override
    public void hideInvalidAmountError() {
        EditText tbxAmount = findViewById(R.id.tbx_amount_deposit_funds);
        ErrorBorder.removeStroke(tbxAmount);
        txtDepositAmountError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingBankNameError(String message) {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBankName);
        txtDepositBankNameError.setVisibility(View.VISIBLE);
        txtDepositBankNameError.setText(message);
    }

    @Override
    public void hideMissingBankNameError() {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_deposit_funds);
        ErrorBorder.removeStroke(tbxBankName);
        txtDepositBankNameError.setVisibility(View.GONE);
    }

    @Override
    public void showInvalidBankNameError(String message) {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBankName);
        txtDepositBankNameError.setVisibility(View.VISIBLE);
        txtDepositBankNameError.setText(message);
    }

    @Override
    public void hideInvalidBankNameError() {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_deposit_funds);
        ErrorBorder.removeStroke(tbxBankName);
        txtDepositBankNameError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingBranchCodeError(String message) {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBranchCode);
        txtDepositBranchCodeError.setVisibility(View.VISIBLE);
        txtDepositBranchCodeError.setText(message);
    }

    @Override
    public void hideMissingBranchCodeError() {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_deposit_funds);
        ErrorBorder.removeStroke(tbxBranchCode);
        txtDepositBranchCodeError.setVisibility(View.GONE);
    }

    @Override
    public void showInvalidBranchCodeError(String message) {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBranchCode);
        txtDepositBranchCodeError.setVisibility(View.VISIBLE);
        txtDepositBranchCodeError.setText(message);
    }

    @Override
    public void hideInvalidBranchCodeError() {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_deposit_funds);
        ErrorBorder.removeStroke(tbxBranchCode);
        txtDepositBranchCodeError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingAccountNumberError(String message) {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountNumber);
        txtDepositAccountNumberError.setVisibility(View.VISIBLE);
        txtDepositAccountNumberError.setText(message);
    }

    @Override
    public void hideMissingAccountNumberError() {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_deposit_funds);
        ErrorBorder.removeStroke(tbxAccountNumber);
        txtDepositAccountNumberError.setVisibility(View.GONE);
    }

    @Override
    public void showInvalidAccountNumberError(String message) {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountNumber);
        txtDepositAccountNumberError.setVisibility(View.VISIBLE);
        txtDepositAccountNumberError.setText(message);
    }

    @Override
    public void hideInvalidAccountNumberError() {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_deposit_funds);
        ErrorBorder.removeStroke(tbxAccountNumber);
        txtDepositAccountNumberError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingAccountHolderError(String message) {
        EditText tbxAccountHolder = findViewById(R.id.tbx_name_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountHolder);
        txtDepositAccountHolderError.setVisibility(View.VISIBLE);
        txtDepositAccountHolderError.setText(message);
    }
    @Override
    public void hideMissingAccountHolderError() {
        EditText tbxAccountHolder = findViewById(R.id.tbx_name_deposit_funds);
        ErrorBorder.removeStroke(tbxAccountHolder);
        txtDepositAccountHolderError.setVisibility(View.GONE);
    }
    @Override
    public void showInvalidAccountHolderError(String message) {
        EditText tbxAccountHolder = findViewById(R.id.tbx_name_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountHolder);
        txtDepositAccountHolderError.setVisibility(View.VISIBLE);
        txtDepositAccountHolderError.setText(message);
    }
    @Override
    public void hideInvalidAccountHolderError() {
        EditText tbxAccountHolder = findViewById(R.id.tbx_name_deposit_funds);
        ErrorBorder.removeStroke(tbxAccountHolder);
        txtDepositAccountHolderError.setVisibility(View.GONE);
    }
    @Override
    public void showMissingExpiryDateError(String message) {
        EditText tbxExpiryDate = findViewById(R.id.tbx_expiry_date_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxExpiryDate);
        txtDepositExpiryDateError.setVisibility(View.VISIBLE);
        txtDepositExpiryDateError.setText(message);
    }
    @Override
    public void hideMissingExpiryDateError() {
        EditText tbxExpiryDate = findViewById(R.id.tbx_expiry_date_deposit_funds);
        ErrorBorder.removeStroke(tbxExpiryDate);
        txtDepositExpiryDateError.setVisibility(View.GONE);
    }
    @Override
    public void showInvalidExpiryDateError(String message) {
        EditText tbxExpiryDate = findViewById(R.id.tbx_expiry_date_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxExpiryDate);
        txtDepositExpiryDateError.setVisibility(View.VISIBLE);
        txtDepositExpiryDateError.setText(message);
    }
    @Override
    public void hideInvalidExpiryDateError() {
        EditText tbxExpiryDate = findViewById(R.id.tbx_expiry_date_deposit_funds);
        ErrorBorder.removeStroke(tbxExpiryDate);
        txtDepositExpiryDateError.setVisibility(View.GONE);
    }
    @Override
    public void showMissingCvvError(String message) {
        EditText tbxCvv = findViewById(R.id.tbx_cvv_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxCvv);
        txtDepositCvvError.setVisibility(View.VISIBLE);
        txtDepositCvvError.setText(message);
    }
    @Override
    public void hideMissingCvvError() {
        EditText tbxCvv = findViewById(R.id.tbx_cvv_deposit_funds);
        ErrorBorder.removeStroke(tbxCvv);
        txtDepositCvvError.setVisibility(View.GONE);
    }
    @Override
    public void showInvalidCvvError(String message) {
        EditText tbxCvv = findViewById(R.id.tbx_cvv_deposit_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxCvv);
        txtDepositCvvError.setVisibility(View.VISIBLE);
        txtDepositCvvError.setText(message);
    }
    @Override
    public void hideInvalidCvvError() {
        EditText tbxCvv = findViewById(R.id.tbx_cvv_deposit_funds);
        ErrorBorder.removeStroke(tbxCvv);
        txtDepositCvvError.setVisibility(View.GONE);
    }


    @Override
    public void finishActivity() {
        finish();
    }
    @Override
    public void showLoadingSpinner() {
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoadingSpinner() {
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.GONE);
            }
        });

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
