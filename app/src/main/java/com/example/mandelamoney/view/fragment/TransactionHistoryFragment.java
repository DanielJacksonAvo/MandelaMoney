package com.example.mandelamoney.view.fragment;

import android.os.Bundle;
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
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.ITransactionHistoryView;

import java.util.ArrayList;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller.TransactionHistoryController.handleLoadUserToUI();
        setupRecycler(view);
        loadOrFetchTransactions();
    }

    private void loadOrFetchTransactions() {
            controller.TransactionHistoryController.refreshAndDisplayTransactions();
        // Use a Handler to post updates to the main UI thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String currentUserEmail = UserSession.getUser().getUserEmail();
            Context context = requireContext(); // Use requireContext() as it ensures context is not null
            Log.d(TAG, "Fetching transaction history for user: " + currentUserEmail + " Context: " + context);
            List<TransactionDetails> transactionList = MySQLConnector.getTransactionHistory(currentUserEmail, context);

            Log.d(TAG, "Fetched transaction count: " + transactionList.size());

            // Post UI updates back to the main thread
            mainHandler.post(() -> {
                UserSession.setCachedTransactionHistory(transactionList); // Cache the data
                if (adapter != null) {
                    adapter.updateData(transactionList); // Update the adapter
                } else {
                    // This case should ideally not happen if setupRecycler is called first,
                    // but as a fallback, initialize and set the adapter.
                    adapter = new TransactionAdapter(transactionList, UserSession.getUser().getUserEmail());
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }

    private void setupRecycler(View rootView) {
        recyclerView = rootView.findViewById(R.id.recyclerView_transactionHistory);
        if (recyclerView == null) {
            Log.e(TAG, "recyclerView is null. Check your layout XML.");
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter == null) {
            adapter = new TransactionAdapter(new ArrayList<>(), UserSession.getUser().getUserEmail());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void displayUserName(String name) {
        if (!checkTablet()) {
            TextView txtUserName = requireView().findViewById(R.id.txt_user_name_transaction_history);
            txtUserName.setText(name.toUpperCase());
        }

    }

    @Override
    public void updateData(List<TransactionDetails> formattedList) {
        if (adapter != null) {
            adapter.updateData(formattedList);
        } else {
            adapter = new TransactionAdapter(formattedList, UserSession.getUser().getUserEmail());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called. Refreshing transactions.");
        loadOrFetchTransactions();
    }


    public boolean checkTablet() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }
}