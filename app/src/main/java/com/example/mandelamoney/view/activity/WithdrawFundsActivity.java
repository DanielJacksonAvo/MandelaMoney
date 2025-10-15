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
import com.example.mandelamoney.controller.WithdrawFundsController;
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IWithdrawFundsView;

public class WithdrawFundsActivity extends AppCompatActivity implements IWithdrawFundsView {
    private WithdrawFundsController controller;
    TextView txtWithdrawAmountError;
    TextView txtWithdrawBankNameError;
    TextView txtWithdrawBranchCodeError;
    TextView txtWithdrawAccountNumberError;
    TextView txtWithdrawAccountHolderError;

    private ConstraintLayout loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
        EditText tbxName = findViewById(R.id.tbx_account_holder_withdraw_funds);
        Button btnWithdrawFunds = findViewById(R.id.btn_withdraw_funds);
        txtWithdrawAmountError = findViewById(R.id.txt_error_amount_withdraw_funds);
        txtWithdrawBankNameError = findViewById(R.id.txt_error_bank_name_withdraw_funds);
        txtWithdrawBranchCodeError = findViewById(R.id.txt_error_branch_code_withdraw_funds);
        txtWithdrawAccountNumberError = findViewById(R.id.txt_error_account_number_withdraw_funds);
        txtWithdrawAccountHolderError = findViewById(R.id.txt_error_account_holder_withdraw_funds);
        configureCancelButton(btnCancel);
        configureWithdrawFundsButton(btnWithdrawFunds, tbxAmount, tbxBankName, tbxBranchCode, tbxCardNumber, tbxName);
        loadingSpinner = findViewById(R.id.withdraw_funds_loading_spinner);
    }

    private void configureWithdrawFundsButton(
            Button btnWithdrawFunds,
            EditText tbxAmount,
            EditText tbxBankName,
            EditText tbxBranchCode,
            EditText tbxCardNumber,
            EditText tbxName
    ) {
        btnWithdrawFunds.setOnClickListener(v -> controller.handleWithdrawFunds(
                tbxAmount.getText().toString().trim(),
                tbxBankName.getText().toString(),
                tbxBranchCode.getText().toString(),
                tbxCardNumber.getText().toString(),
                tbxName.getText().toString()
        ));
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCancelWithdrawFunds());
    }
    @Override
    public void showMissingAmountError(String message) {
        EditText tbxAmount = findViewById(R.id.tbx_amount_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAmount);
        txtWithdrawAmountError.setVisibility(View.VISIBLE);
        txtWithdrawAmountError.setText(message);

    }

    @Override
    public void hideMissingAmountError() {
        EditText tbxAmount = findViewById(R.id.tbx_amount_withdraw_funds);
        ErrorBorder.removeStroke(tbxAmount);
        txtWithdrawAmountError.setVisibility(View.GONE);

    }

    @Override
    public void showInvalidAmountError(String message) {
        EditText tbxAmount = findViewById(R.id.tbx_amount_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAmount);
        txtWithdrawAmountError.setVisibility(View.VISIBLE);
        txtWithdrawAmountError.setText(message);

    }
    @Override
    public void hideInvalidAmountError() {
        EditText tbxAmount = findViewById(R.id.tbx_amount_withdraw_funds);
        ErrorBorder.removeStroke(tbxAmount);
        txtWithdrawAmountError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingBankNameError(String message) {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBankName);
        txtWithdrawBankNameError.setVisibility(View.VISIBLE);
        txtWithdrawBankNameError.setText(message);
    }

    @Override
    public void hideMissingBankNameError() {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_withdraw_funds);
        ErrorBorder.removeStroke(tbxBankName);
        txtWithdrawBankNameError.setVisibility(View.GONE);
    }

    @Override
    public void showInvalidBankNameError(String message) {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBankName);
        txtWithdrawBankNameError.setVisibility(View.VISIBLE);
        txtWithdrawBankNameError.setText(message);
    }

    @Override
    public void hideInvalidBankNameError() {
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_withdraw_funds);
        ErrorBorder.removeStroke(tbxBankName);
        txtWithdrawBankNameError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingBranchCodeError(String message) {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBranchCode);
        txtWithdrawBranchCodeError.setVisibility(View.VISIBLE);
        txtWithdrawBranchCodeError.setText(message);
    }

    @Override
    public void hideMissingBranchCodeError() {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_withdraw_funds);
        ErrorBorder.removeStroke(tbxBranchCode);
        txtWithdrawBranchCodeError.setVisibility(View.GONE);
    }

    @Override
    public void showInvalidBranchCodeError(String message) {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxBranchCode);
        txtWithdrawBranchCodeError.setVisibility(View.VISIBLE);
        txtWithdrawBranchCodeError.setText(message);
    }

    @Override
    public void hideInvalidBranchCodeError() {
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_withdraw_funds);
        ErrorBorder.removeStroke(tbxBranchCode);
        txtWithdrawBranchCodeError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingAccountNumberError(String message) {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountNumber);
        txtWithdrawAccountNumberError.setVisibility(View.VISIBLE);
        txtWithdrawAccountNumberError.setText(message);
    }

    @Override
    public void hideMissingAccountNumberError() {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_withdraw_funds);
        ErrorBorder.removeStroke(tbxAccountNumber);
        txtWithdrawAccountNumberError.setVisibility(View.GONE);
    }

    @Override
    public void showInvalidAccountNumberError(String message) {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountNumber);
        txtWithdrawAccountNumberError.setVisibility(View.VISIBLE);
        txtWithdrawAccountNumberError.setText(message);
    }

    @Override
    public void hideInvalidAccountNumberError() {
        EditText tbxAccountNumber = findViewById(R.id.tbx_card_number_withdraw_funds);
        ErrorBorder.removeStroke(tbxAccountNumber);
        txtWithdrawAccountNumberError.setVisibility(View.GONE);
    }

    @Override
    public void showMissingAccountHolderError(String message) {
        EditText tbxAccountHolder = findViewById(R.id.tbx_account_holder_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountHolder);
        txtWithdrawAccountHolderError.setVisibility(View.VISIBLE);
        txtWithdrawAccountHolderError.setText(message);
    }
    @Override
    public void hideMissingAccountHolderError() {
        EditText tbxAccountHolder = findViewById(R.id.tbx_account_holder_withdraw_funds);
        ErrorBorder.removeStroke(tbxAccountHolder);
        txtWithdrawAccountHolderError.setVisibility(View.GONE);
    }
    @Override
    public void showInvalidAccountHolderError(String message) {
        EditText tbxAccountHolder = findViewById(R.id.tbx_account_holder_withdraw_funds);
        ErrorBorder.applyMandelaYellowBorder(tbxAccountHolder);
        txtWithdrawAccountHolderError.setVisibility(View.VISIBLE);
        txtWithdrawAccountHolderError.setText(message);
    }
    @Override
    public void hideInvalidAccountHolderError() {
        EditText tbxAccountHolder = findViewById(R.id.tbx_account_holder_withdraw_funds);
        ErrorBorder.removeStroke(tbxAccountHolder);
        txtWithdrawAccountHolderError.setVisibility(View.GONE);
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

    @Override
    public void finishActivity() {
        finish();
    }

}