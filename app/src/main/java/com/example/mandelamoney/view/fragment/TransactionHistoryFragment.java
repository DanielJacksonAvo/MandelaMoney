package com.example.mandelamoney.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ConstraintLayout filterBarContainer;
    private ConstraintLayout searchContainer;
    private EditText etSearch;
    private ImageView iconSearch;

    private boolean isSearchExpanded = false;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;


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
        filterBarContainer = view.findViewById(R.id.filter_bar_container);
        searchContainer = view.findViewById(R.id.search_container);
        etSearch = view.findViewById(R.id.et_search);
        iconSearch = view.findViewById(R.id.icon_search);
        searchContainer.setOnClickListener(v -> toggleSearchBar());
        initSearchListener();

        setupRecycler(view);
        loadOrFetchTransactions();

    }

    private void loadOrFetchTransactions() {
            controller.TransactionHistoryController.refreshAndDisplayTransactions();
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
    private void toggleSearchBar() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(filterBarContainer);

        // Clear any previous percent width (use weights instead)
        constraintSet.clear(R.id.search_container, ConstraintSet.START);
        constraintSet.clear(R.id.btn_period, ConstraintSet.START);
        constraintSet.clear(R.id.btn_type, ConstraintSet.START);

        constraintSet.connect(R.id.search_container, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(R.id.btn_period, ConstraintSet.START, R.id.search_container, ConstraintSet.END);
        constraintSet.connect(R.id.btn_type, ConstraintSet.START, R.id.btn_period, ConstraintSet.END);
        constraintSet.connect(R.id.btn_type, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        constraintSet.connect(R.id.search_container, ConstraintSet.END, R.id.btn_period, ConstraintSet.START);
        constraintSet.connect(R.id.btn_period, ConstraintSet.END, R.id.btn_type, ConstraintSet.START);

        constraintSet.setHorizontalChainStyle(R.id.search_container, ConstraintSet.CHAIN_SPREAD);

        if (!isSearchExpanded) {
            constraintSet.setHorizontalWeight(R.id.search_container, 0.6f);
            constraintSet.setHorizontalWeight(R.id.btn_period, 0.2f);
            constraintSet.setHorizontalWeight(R.id.btn_type, 0.2f);
            etSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        } else {
            constraintSet.setHorizontalWeight(R.id.search_container, 0.13f);
            constraintSet.setHorizontalWeight(R.id.btn_period, 0.43f);
            constraintSet.setHorizontalWeight(R.id.btn_type, 0.44f);
            etSearch.setText("");
            etSearch.setVisibility(View.GONE);
        }

        TransitionManager.beginDelayedTransition(filterBarContainer);
        constraintSet.applyTo(filterBarContainer);
        isSearchExpanded = !isSearchExpanded;
    }


    @Override
    public void displayUserName(String name) {
        TextView txtUserName = requireView().findViewById(R.id.txt_user_name_transaction_history);
        txtUserName.setText(name.toUpperCase());
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
    private void initSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    String query = s.toString().trim();
                    controller.TransactionHistoryController.queryWithFilters(query, null, null); // will update below
                };
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


}