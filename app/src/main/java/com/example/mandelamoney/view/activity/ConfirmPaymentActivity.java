package com.example.mandelamoney.view.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.MakePaymentController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IConfirmPaymentView;

public class ConfirmPaymentActivity extends AppCompatActivity implements IConfirmPaymentView {

    private MakePaymentController makePaymentController;
    private ConstraintLayout loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_payment);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        Object obj = DataShare.receive();
        if (obj instanceof MakePaymentController) {
            makePaymentController = (MakePaymentController) obj;
            makePaymentController.setConfirmPaymentView(this);
            makePaymentController.setContext(this);
        }
        connectToUI();
        makePaymentController.loadConfirmPaymentData();

    }


    private void connectToUI() {
        Button btnConfirm = findViewById(R.id.btn_confirmpayment);
        TextView btnCancel = findViewById(R.id.btn_confirmpayment_cancel);
        loadingSpinner = findViewById(R.id.confirm_loading_spinner);
        configureConfirmButton(btnConfirm);
        configureCancelButton(btnCancel);
    }

    private void configureConfirmButton(Button btnConfirm) {
        if (makePaymentController != null) {
            btnConfirm.setOnClickListener((view) -> makePaymentController.handleConfirmPayment());
        }
    }

    private void configureCancelButton(TextView btnCancel) {
        if (makePaymentController != null) {
            btnCancel.setOnClickListener((view) -> makePaymentController.handleCancel());
        }
    }

    @Override
    public void displayToUserName(String name) {
        TextView tbx = findViewById(R.id.txt_toname_confirmpayment);
        tbx.setText(name);
    }

    @Override
    public void displayFromUserName(String name) {
        TextView tbx = findViewById(R.id.txt_fromname_confirmpayment);
        tbx.setText(name);
    }

    @Override
    public void displayToUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_tonumber_confirmpayment);
        tbx.setText(number);
    }

    @Override
    public void displayFromUserNumber(String number) {
        TextView tbx = findViewById(R.id.txt_fromnumber_confirmpayment);
        tbx.setText(number);
    }

    @Override
    public void displayToUserTransactionType(String type) {
        TextView tbx = findViewById(R.id.txt_transactiontypeto_confirmpayment);
        tbx.setText(type);
    }

    @Override
    public void displayFromUserTransactionType(String type) {
        TextView tbx = findViewById(R.id.txt_transactiontypefrom_confirmpayment);
        tbx.setText(type);
    }

    @Override
    public void displayAmount(double amount) {
        TextView tbx = findViewById(R.id.txt_amount_confirmpayment);
        @SuppressLint("DefaultLocale") String stringAmount = "R" + String.format("%.2f",amount);
        tbx.setText(stringAmount);    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showLoadingSpinner() {
        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void hideLoadingSpinner() {
        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(View.GONE);
        }
    }


}