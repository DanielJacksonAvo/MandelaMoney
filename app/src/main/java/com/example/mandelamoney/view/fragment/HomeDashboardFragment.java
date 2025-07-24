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
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;

import java.util.Locale;

public class HomeDashboardFragment extends Fragment implements IHomeDashboardView {

    DashboardController controller;
    private TextView txtBalance, txtUserName;
    // ... other class members

    public HomeDashboardFragment(DashboardController controller) {
        this.controller = controller;
        controller.createDashboardHomeController(this);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectToUI(view);
        controller.DashboardHomeController.handleLoadUserToUI();
    }

    private void connectToUI(View rootView) {
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
            if (getActivity() instanceof AppCompatActivity) {
                DashboardController.TransactionHistoryController transactionController =
                        new DashboardController.TransactionHistoryController((AppCompatActivity) getActivity());
                transactionController.handleViewTransactionHistory();
            }
        });
    }


    @Override
    public void displayBalance(double balance) {
        String display = "R " + String.format(Locale.getDefault(), "%.2f", balance);
        txtBalance.setText(display);
    }

    @Override
    public void displayUserName(String name) {
        txtUserName.setText(toUpperCase(name));
    }


    private void configureRequestPayButton(Button btnRequestPay) {
        btnRequestPay.setOnClickListener((view) -> {
            controller.DashboardHomeController.handleRequestPayment();
        });
    }

    private void configurePayNowButton(Button btnPayNow){
            btnPayNow.setOnClickListener((view)->controller.DashboardHomeController.handleMakePayment());
    }
}
