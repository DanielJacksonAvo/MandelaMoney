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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DepositFundsController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.UserSession;
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
        // Try restore user via DataShare (if provided)
        Object payload = DataShare.receive();
        if (UserSession.getUser() == null && payload instanceof com.example.mandelamoney.model.User) {
            UserSession.setUser((com.example.mandelamoney.model.User) payload);
            Log.d("DepositFundsActivity", "Restored user from DataShare.");
        }

        // Session gate
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

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setController();   // see next step
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

    private void configureDepositFundsButton(
            Button btnDepositFunds,
            EditText tbxAmount,
            EditText tbxBankName,
            EditText tbxBranchCode,
            EditText tbxCardNumber,
            EditText tbxExpiryDate,
            EditText tbxCVV,
            EditText tbxName
    ) {
        btnDepositFunds.setOnClickListener(v -> {
            Float amount = null;
            String amtTxt = tbxAmount.getText().toString().trim();
            if (!amtTxt.isEmpty()) {
                try { amount = Float.parseFloat(amtTxt); } catch (NumberFormatException ignored) { /* keep null */ }
            }

            controller.handleDepositFunds(
                    amount,
                    tbxBankName.getText().toString(),
                    tbxBranchCode.getText().toString(),
                    tbxCardNumber.getText().toString(),
                    tbxName.getText().toString(),
                    tbxExpiryDate.getText().toString(),
                    tbxCVV.getText().toString()
            );
        });
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
            int cursor = editText.getSelectionStart();

            // Keep only digits, max MMYYYY (6 digits)
            String digits = raw.replaceAll("\\D", "");
            if (digits.length() > 6) digits = digits.substring(0, 6);

            String formatted;
            boolean rawHadSlash = raw.contains("/");
            boolean insertingSlashNow = false;

            if (digits.length() < 2) {
                // 0–1 digits: just show them
                formatted = digits;
            } else if (digits.length() == 2) {
                // Exactly MM -> auto-insert slash
                formatted = digits + "/";
                insertingSlashNow = !rawHadSlash; // we're adding it now
            } else { // 3–6 digits
                // MM/YYYY
                formatted = digits.substring(0, 2) + "/" + digits.substring(2);
                // If raw had no slash yet, this is the moment we insert it
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
