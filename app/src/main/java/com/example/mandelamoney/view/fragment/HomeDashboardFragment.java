package com.example.mandelamoney.view.fragment;

import static android.icu.lang.UCharacter.toUpperCase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.controller.MakePaymentController;
import com.example.mandelamoney.controller.RequestPaymentController;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;

import java.util.Locale;

public class HomeDashboardFragment extends Fragment implements IHomeDashboardView {

    DashboardController controller;
    DashboardController.TransactionHistoryController transactionController;
    private TextView txtBalance, txtUserName;
    private EditText tbxRequestPayAmount;

    // ... other class members

    public HomeDashboardFragment(DashboardController controller) {
        this.controller = controller;
        controller.createDashboardHomeController(this);

    }

    public boolean checkTablet() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (checkTablet()) {
            connectToUITablet(view);
        } else {
            connectToUIMobile(view);
        }
        controller.DashboardHomeController.handleLoadUserToUI();
    }

    private void connectToUIMobile(View rootView) {
        txtBalance = rootView.findViewById(R.id.txt_user_account_balance);
        txtUserName = rootView.findViewById(R.id.txt_user_name_dashboard);
        Button btnRequestPay = rootView.findViewById(R.id.btn_request_pay_dashboard);
        configureRequestPayButton(btnRequestPay);
        TextView btnTransactionHistory = rootView.findViewById(R.id.txt_transaction_history_dashboard);
        configureTransactionHistoryButton(btnTransactionHistory);
        Button btnPayNow = rootView.findViewById(R.id.btn_pay_now);
        configurePayNowButton(btnPayNow);
    }

    private void configureTransactionHistoryButton(TextView btnTransactionHistory) {
        btnTransactionHistory.setOnClickListener((view) -> {
            controller.handleViewTransactionHistory();
        });
    }

    private void connectToUITablet(View rootView) {
        txtBalance = rootView.findViewById(R.id.txt_user_account_balance);
        Button btnWithdraw = rootView.findViewById(R.id.btn_withdraw);
        congifureWithdrawButton(btnWithdraw);
        Button btnPayNow = rootView.findViewById(R.id.btn_pay_now);
        configurePayNowButton(btnPayNow);
        tbxRequestPayAmount = rootView.findViewById(R.id.tbx_amount_request_payment);
        Button btnGenerateQR = rootView.findViewById(R.id.btn_generate_qr_request_payment);
        configureGenerateQRCodeButton(btnGenerateQR);
    }

    private void congifureWithdrawButton(Button btnWithdraw) {

    }

    @Override
    public void displayBalance(double balance) {
        String display = "R " + String.format(Locale.getDefault(), "%.2f", balance);
        txtBalance.setText(display);
    }

    @Override
    public void displayUserName(String name) {
        if (txtUserName != null && !checkTablet()) {
            txtUserName.setText(toUpperCase(name));
        }
    }


    private void configureRequestPayButton(Button btnRequestPay) {
        if (btnRequestPay != null && !checkTablet()) {
            btnRequestPay.setOnClickListener((view) -> {
                controller.DashboardHomeController.handleRequestPayment();
            });
        }

    }

    private void configurePayNowButton(Button btnPayNow) {
        if (btnPayNow != null) {
            btnPayNow.setOnClickListener((view) -> controller.DashboardHomeController.handleMakePayment());

        }
    }

    private void configureGenerateQRCodeButton(Button btnGenerateQR) {
        if (btnGenerateQR != null && checkTablet()) {
            btnGenerateQR.setOnClickListener((view) -> {
                RequestPaymentController requestPaymentController = new RequestPaymentController(getContext(), this);
                requestPaymentController.handleGenerateQR(tbxRequestPayAmount.getText().toString());
            });

        }
    }
    @Override
    public void onResume() {
        super.onResume();
        controller.DashboardHomeController.handleLoadUserToUI(); // rebinding + balance refresh
    }

}
