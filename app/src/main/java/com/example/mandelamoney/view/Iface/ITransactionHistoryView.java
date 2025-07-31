package com.example.mandelamoney.view.Iface;

import com.example.mandelamoney.model.Transaction;

import java.util.List;

public interface ITransactionHistoryView {
    void displayUserName(String name);

    void updateData(List<Transaction> formattedList);
    String[] getFilters();
}

