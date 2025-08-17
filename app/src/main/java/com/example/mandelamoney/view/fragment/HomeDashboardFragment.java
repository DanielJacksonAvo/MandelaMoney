package com.example.mandelamoney.view.fragment;

import static android.icu.lang.UCharacter.toUpperCase;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.adapter.TransactionAdapter;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.controller.RequestPaymentController;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;
import com.example.mandelamoney.view.activity.RequestPaymentEnterAmountActivity;
import com.example.mandelamoney.view.activity.ShowFailedActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeDashboardFragment extends Fragment implements IHomeDashboardView {

    DashboardController controller;
    private TextView txtBalance, txtUserName;
    private EditText tbxRequestPayAmount;
    private TransactionAdapter adapter;
    private RecyclerView recyclerView;

    private boolean isTabletLandscape = false;

    public HomeDashboardFragment() {

    }

    public void setController(DashboardController controller) {
        this.controller = controller;
        if (this.controller != null) {
            this.controller.createDashboardHomeController(this);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTabletLandscape = getResources().getBoolean(R.bool.is_tablet_landscape);
    }

    public boolean checkTablet() {
        return isTabletLandscape;
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
        recyclerView = rootView.findViewById(R.id.recyclerView_dashboard_transactionHistory);
        setupRecycler();
    }

    private void configureTransactionHistoryButton(TextView btnTransactionHistory) {
        btnTransactionHistory.setOnClickListener((view) -> controller.handleViewTransactionHistory());
    }

    private void setupRecycler() {
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            User user = UserSession.getUser();

            if (user == null) {
                Log.e("HomeDashboardFragment", "User is null in setupRecycler()");
                return;
            }

            if (adapter == null) {
                adapter = new TransactionAdapter(new ArrayList<>(), user.getUserEmail());
                recyclerView.setAdapter(adapter);
            }
        }
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
    public void displayBalance(float balance) {
        String display = "R " + String.format(Locale.getDefault(), "%.2f", balance);
        txtBalance.setText(display);
    }

    @Override
    public void displayUserName(String name) {
        if (txtUserName != null && !checkTablet()) {
            txtUserName.setText(toUpperCase(name));
        }
    }

    @Override
    public void displayTransactions(List<Transaction> transactionDetails) {
        if (recyclerView != null && !checkTablet()) {
            if (adapter != null) {
                adapter.updateData(transactionDetails);
            } else {
                adapter = new TransactionAdapter(transactionDetails, UserSession.getUser().getUserEmail());
                recyclerView.setAdapter(adapter);
            }
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
                RequestPaymentController requestPaymentController = new RequestPaymentController();
                requestPaymentController.setContext(getContext());
                requestPaymentController.handleGenerateQR(tbxRequestPayAmount.getText().toString());
            });

        }
    }
    @Override
    public void onResume() {
        super.onResume();
        controller.DashboardHomeController.handleLoadUserToUI();
    }
}