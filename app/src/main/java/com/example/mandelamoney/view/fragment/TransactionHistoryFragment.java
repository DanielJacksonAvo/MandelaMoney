package com.example.mandelamoney.view.fragment;

import android.content.Context;
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
    private final DashboardController controller;
    private static final String TAG = "TransactionHistoryDebug";


    public TransactionHistoryFragment(DashboardController controller) {
        this.controller = controller;
        controller.createTransactionHistoryController(this);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (recyclerView == null){
            Log.d("TransactionHistory", "recyclerView is null");
            return;
        }
        UserSession.refreshTransactionHistory(requireContext(), () -> {
            List<TransactionDetails> updatedList = UserSession.getCachedTransactionHistory();

            if (adapter != null) {
                adapter.updateData(updatedList);
            } else {
                adapter = new TransactionAdapter(updatedList, UserSession.getUser().getUserEmail());
                recyclerView.setAdapter(adapter);
            }
        });
        controller.TransactionHistoryController.handleLoadUserToUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView_transactionHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load user name into header
        controller.TransactionHistoryController.handleLoadUserToUI();

        // Load or fetch transactions
        loadOrFetchTransactions();
    }

    private void loadOrFetchTransactions() {
        new Thread(() -> {
            String currentUserEmail = UserSession.getUser().getUserEmail();
            Context context = requireContext();
            Log.d("TransactionHistory", "Fetching transaction history for user: " + currentUserEmail + " Context: " + context.toString());
            List<TransactionDetails> transactionList = MySQLConnector.getTransactionHistory(currentUserEmail, requireContext());

            Log.d("TransactionHistory", "Fetched transaction count: " + transactionList.size());

            UserSession.setCachedTransactionHistory(transactionList);

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
    @Override
    public void onResume() {
        super.onResume();
        Log.d("TransactionHistory", "onResume called");
        if (recyclerView == null) return;
        UserSession.refreshTransactionHistory(requireContext(), () -> {
            List<TransactionDetails> updatedList = UserSession.getCachedTransactionHistory();
            if (adapter != null) {
                adapter.updateData(updatedList);
            } else {
                adapter = new TransactionAdapter(updatedList, UserSession.getUser().getUserEmail());
                recyclerView.setAdapter(adapter);
            }
        });
        controller.TransactionHistoryController.handleLoadUserToUI();

    }
}
