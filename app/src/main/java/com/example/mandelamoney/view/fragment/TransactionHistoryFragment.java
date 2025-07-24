package com.example.mandelamoney.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.adapter.TransactionAdapter;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.model.TransactionDetails;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.ITransactionHistoryView;

import java.util.List;

public class TransactionHistoryFragment extends Fragment implements ITransactionHistoryView {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private final DashboardController.TransactionHistoryController controller;

    public TransactionHistoryFragment(DashboardController.TransactionHistoryController controller) {
        this.controller = controller;
        this.controller.createTransactionHistoryController(this); // bind interface
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init UI
        recyclerView = view.findViewById(R.id.recyclerView_transactionHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set user name
        controller.handleLoadUserToUI();

        // Load transactions
        new Thread(() -> {
            String currentUserEmail = UserSession.getUser().getUserEmail();
            List<TransactionDetails> transactionList = MySQLConnector.getTransactionHistory(currentUserEmail, getContext());
            Log.d("TransactionHistory", "List size: " + transactionList.size());

            new Handler(Looper.getMainLooper()).post(() -> {
                adapter = new TransactionAdapter(transactionList, currentUserEmail);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    @Override
    public void displayUserName(String name) {
        TextView txtUserName = requireView().findViewById(R.id.txt_user_name_transaction_history);
        txtUserName.setText(name.toUpperCase());
    }
}
