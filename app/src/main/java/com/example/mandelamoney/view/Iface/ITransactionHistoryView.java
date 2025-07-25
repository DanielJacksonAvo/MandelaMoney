package com.example.mandelamoney.view.Iface;

import com.example.mandelamoney.model.TransactionDetails;

import java.util.List;

public interface ITransactionHistoryView {
    void displayUserName(String name);

    void updateData(List<TransactionDetails> formattedList);
}

