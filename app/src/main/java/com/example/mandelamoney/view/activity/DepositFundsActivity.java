package com.example.mandelamoney.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DepositFundsController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IDepositFundsView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;

public class DepositFundsActivity extends AppCompatActivity implements IDepositFundsView {
    private DepositFundsController controller;
    TextView txtDepositFundsError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deposit_funds);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setController();
        connectToUI();

    }
    private void setController() {
        controller = (DepositFundsController) DataShare.receive();
    }
    private void connectToUI() {
        TextView btnCancel = findViewById(R.id.btn_cancel_deposit_funds);
        EditText tbxAmount = findViewById(R.id.tbx_amount_deposit_funds);
        EditText tbxBankName = findViewById(R.id.tbx_bank_name_deposit_funds);
        EditText tbxBranchCode = findViewById(R.id.tbx_branch_code_deposit_funds);
        EditText tbxCardNumber = findViewById(R.id.tbx_card_number_deposit_funds);
        EditText tbxExpiryDate = findViewById(R.id.tbx_expiry_date_deposit_funds);
        EditText tbxCVV = findViewById(R.id.tbx_cvv_deposit_funds);
        EditText tbxName = findViewById(R.id.tbx_name_deposit_funds);
        Button btnDepositFunds = findViewById(R.id.btn_deposit_funds);
        txtDepositFundsError = findViewById(R.id.txt_error_deposit_funds);
        tbxExpiryDate.setKeyListener(DigitsKeyListener.getInstance("0123456789/"));
        tbxExpiryDate.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(7) });
        tbxExpiryDate.addTextChangedListener(new DateSlashTextWatcher(tbxExpiryDate));
        configureCancelButton(btnCancel);
        configureDepositFundsButton(btnDepositFunds, tbxAmount, tbxBankName, tbxBranchCode, tbxCardNumber, tbxExpiryDate, tbxCVV, tbxName);
    }

    private void configureDepositFundsButton(Button btnDepositFunds, EditText tbxAmount, EditText tbxBankName, EditText tbxBranchCode, EditText tbxCardNumber, EditText tbxExpiryDate, EditText tbxCVV, EditText tbxName) {
        btnDepositFunds.setOnClickListener((view) -> controller.handleDepositFunds(Float.valueOf(tbxAmount.getText().toString()),String.valueOf(tbxBankName.getText()),String.valueOf(tbxBranchCode.getText()),String.valueOf(tbxCardNumber.getText()),String.valueOf(tbxName.getText()),String.valueOf(tbxExpiryDate.getText()),tbxCVV.getText().toString()));
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCancelDepositFunds());
    }


    @Override
    public void showMissingFieldError(String message) {
        txtDepositFundsError.setVisibility(View.VISIBLE);
        txtDepositFundsError.setText(message);

    }

    @Override
    public void hideMissingFieldError() {
        txtDepositFundsError.setVisibility(View.GONE);

    }

    @Override
    public void showInvalidFieldError(String message) {
        txtDepositFundsError.setVisibility(View.VISIBLE);
        txtDepositFundsError.setText(message);

    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void hideInvalidFieldError() {
        txtDepositFundsError.setVisibility(View.GONE);
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

            String digits = raw.replaceAll("[^\\d]", "");
            if (digits.length() > 6) digits = digits.substring(0, 6); // dd + yyyy

            StringBuilder out = new StringBuilder();
            int cursor = editText.getSelectionStart();

            if (digits.length() <= 2) {
                out.append(digits);
            } else {
                out.append(digits.substring(0, 2)).append('/');
                out.append(digits.substring(2));
            }

            String formatted = out.toString();
            if (!formatted.equals(raw)) {
                selfChange = true;
                editText.setText(formatted);
                int newPos = cursor;
                if (digits.length() == 2 && !raw.contains("/")) {
                    newPos = 3;
                } else {
                    newPos = Math.min(formatted.length(), Math.max(0, newPos));
                }
                editText.setSelection(newPos);
                selfChange = false;
            }
        }
    }

}
