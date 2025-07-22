package com.example.mandelamoney.view.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.adapter.TransactionAdapter;
import com.example.mandelamoney.model.TransactionDetails;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;

    public TransactionHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_transactionHistory);  // Make sure you have this ID in your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample user email
        String currentUserEmail = "you@example.com"; // replace with real logged-in user

        // Sample data for testing
        List<TransactionDetails> transactionList = new ArrayList<>();
        transactionList.add(new TransactionDetails("alice@example.com", "you@example.com", 250.00f, "2025-07-21", "10:35"));
        transactionList.add(new TransactionDetails("you@example.com", "bob@example.com", 150.50f, "2025-07-20", "14:10"));
        transactionList.add(new TransactionDetails("carol@example.com", "you@example.com", 400.00f, "2025-07-19", "09:45"));

        // Set adapter
        adapter = new TransactionAdapter(transactionList, currentUserEmail);
        recyclerView.setAdapter(adapter);
    }
}
