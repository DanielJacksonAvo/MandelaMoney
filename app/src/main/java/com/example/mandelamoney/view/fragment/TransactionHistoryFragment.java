package com.example.mandelamoney.view.fragment;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.util.Log;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.adapter.TransactionAdapter;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.model.TransactionDetails;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.ITransactionHistoryView;
import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Method;
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
    private MaterialButton btnPeriod, btnType;
    private String selectedPeriod = null;
    private String selectedType = null;

    public TransactionHistoryFragment(DashboardController controller) {
        this.controller = controller;
        controller.createTransactionHistoryController(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller.TransactionHistoryController.handleLoadUserToUI();
        setupRecycler(view);
        loadOrFetchTransactions();
        filterBarContainer = view.findViewById(R.id.filter_bar_container);
        searchContainer = view.findViewById(R.id.search_container);
        etSearch = view.findViewById(R.id.et_search);
        iconSearch = view.findViewById(R.id.icon_search);
        searchContainer.setOnClickListener(v -> toggleSearchBar());
        initSearchListener();
        btnPeriod = view.findViewById(R.id.btn_period);
        btnType = view.findViewById(R.id.btn_type);
        ConstraintSet initial = new ConstraintSet();
        initial.clone(filterBarContainer);
        initial.setHorizontalWeight(R.id.search_container, 0.13f);
        initial.setHorizontalWeight(R.id.btn_period, 0.43f);
        initial.setHorizontalWeight(R.id.btn_type, 0.44f);
        initial.applyTo(filterBarContainer);
        setupDropdown(btnPeriod, R.menu.transactionhistoryperiodmenu, "PERIOD:");
        setupDropdown(btnType, R.menu.transactionhistorytypemenu, "TYPE:");
    }

    private void loadOrFetchTransactions() {
        controller.TransactionHistoryController.loadTransactions(null,null,null);
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String currentUserEmail = UserSession.getUser().getUserEmail();
            Context context = requireContext();
            Log.d(TAG, "Fetching transaction history for user: " + currentUserEmail + " Context: " + context);
            List<TransactionDetails> transactionList = MySQLConnector.getTransactionHistory(currentUserEmail, context);

            Log.d(TAG, "Fetched transaction count: " + transactionList.size());
            mainHandler.post(() -> {
                UserSession.setCachedTransactionHistory(transactionList);
                if (adapter != null) {
                    adapter.updateData(transactionList);
                } else {
                    adapter = new TransactionAdapter(transactionList, UserSession.getUser().getUserEmail());
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }

    private void setupRecycler(View rootView) {
        recyclerView = rootView.findViewById(R.id.recyclerView_transactionHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter == null) {
            adapter = new TransactionAdapter(new ArrayList<>(), UserSession.getUser().getUserEmail());
            recyclerView.setAdapter(adapter);
        }
    }

    private void toggleSearchBar() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(filterBarContainer);
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
            constraintSet.setHorizontalWeight(R.id.search_container, 0.55f);
            constraintSet.setHorizontalWeight(R.id.btn_period, 0.225f);
            constraintSet.setHorizontalWeight(R.id.btn_type, 0.225f);
            etSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            convertButtonToIcon(btnPeriod, R.drawable.img_calander_filter_icon);
            convertButtonToIcon(btnType, R.drawable.img_type_filter_icon);
        } else {
            constraintSet.setHorizontalWeight(R.id.search_container, 0.13f);
            constraintSet.setHorizontalWeight(R.id.btn_period, 0.43f);
            constraintSet.setHorizontalWeight(R.id.btn_type, 0.44f);
            etSearch.setText("");
            etSearch.setVisibility(View.GONE);
            restoreFilterButton(btnPeriod, "PERIOD:");
            restoreFilterButton(btnType, "TYPE:");
        }

        TransitionManager.beginDelayedTransition(filterBarContainer);
        constraintSet.applyTo(filterBarContainer);
        isSearchExpanded = !isSearchExpanded;
    }

    private void convertButtonToIcon(MaterialButton button, int iconRes) {
        button.setText("");
        button.setIconResource(iconRes);
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        button.setIconSize(dpToPx(27));
        button.setIconPadding(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setPadding(0, 0, 0, 0);
        button.setIconTintResource(android.R.color.white);
        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.grey10));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void restoreFilterButton(MaterialButton button, String label) {
        button.setText(label);
        button.setIconResource(R.drawable.img_drop_down_icon);
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END);
        button.setIconPadding(dpToPx(8));
        button.setIconTint(null);
        String selected = label.equals("PERIOD:") ? selectedPeriod : selectedType;
        if (selected != null) {
            styleFilterButton(button, label, selected);
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
        loadOrFetchTransactions();
    }

    private void initSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> controller.TransactionHistoryController.loadTransactions(s.toString().trim(), selectedPeriod, selectedType);
                searchHandler.postDelayed(searchRunnable, 400);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @SuppressLint("RestrictedApi")
    private void setupDropdown(MaterialButton button, @MenuRes int menuRes, String label) {
        MenuBuilder menuBuilder = new MenuBuilder(requireContext());
        MenuInflater inflater = new MenuInflater(requireContext());
        inflater.inflate(menuRes, menuBuilder);
        MenuPopupHelper popupHelper = new MenuPopupHelper(requireContext(), menuBuilder, button);
        popupHelper.setForceShowIcon(true);

        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                String selected = item.getTitle().toString();
                if ("PERIOD:".equals(label)) selectedPeriod = selected;
                else if ("TYPE:".equals(label)) selectedType = selected;
                if (!isSearchExpanded) styleFilterButton(button, label, selected);
                String query = etSearch.getText().toString().trim();
                controller.TransactionHistoryController.loadTransactions(query, selectedPeriod, selectedType);
                return true;
            }
            @Override public void onMenuModeChange(@NonNull MenuBuilder menu) {}
        });

        try {
            Method method = menuBuilder.getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            method.setAccessible(true);
            method.invoke(menuBuilder, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        button.setOnClickListener(v -> popupHelper.show());
    }

    private void styleFilterButton(MaterialButton button, String label, String value) {
        SpannableString styled = new SpannableString(label + "\n" + value);
        try {
            Typeface light = ResourcesCompat.getFont(requireContext(), R.font.dm_sans_light);
            Typeface medium = ResourcesCompat.getFont(requireContext(), R.font.dm_sans_medium);
            styled.setSpan(new CustomTypefaceSpan(light), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new RelativeSizeSpan(0.75f), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new CustomTypefaceSpan(medium), label.length() + 1, styled.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new RelativeSizeSpan(0.85f), label.length() + 1, styled.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            button.setText(styled);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class CustomTypefaceSpan extends TypefaceSpan {
        private final Typeface newType;
        public CustomTypefaceSpan(Typeface type) { super(""); this.newType = type; }
        @Override public void updateDrawState(TextPaint ds) { applyCustomTypeFace(ds, newType); }
        @Override public void updateMeasureState(TextPaint paint) { applyCustomTypeFace(paint, newType); }
        private void applyCustomTypeFace(Paint paint, Typeface tf) { paint.setTypeface(tf); }
    }

    public boolean checkTablet() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }
}



