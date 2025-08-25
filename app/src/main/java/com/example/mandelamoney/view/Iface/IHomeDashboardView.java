package com.example.mandelamoney.view.Iface;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mandelamoney.model.Transaction;

import java.util.List;

public interface IHomeDashboardView {
    void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState);

    void displayBalance(float balance);

    void displayUserName(String name);

    void displayTransactions(List<Transaction> transactionDetails);
    void showErrorMessage(String error);
    void hideErrorMessage();
}
