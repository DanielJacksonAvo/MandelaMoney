package com.example.mandelamoney.view.Iface;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.model.TransactionDetails;

import java.util.List;

public interface IHomeDashboardView {
    void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState);

    void displayBalance(double balance);

    void displayUserName(String name);

    void displayTransactions(List<TransactionDetails> transactionDetails);
}
