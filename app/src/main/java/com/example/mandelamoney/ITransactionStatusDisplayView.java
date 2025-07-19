package com.example.mandelamoney;

public interface ITransactionStatusDisplayView {
    void displayToUserName(String name);
    void displayFromUserName(String name);
    void displayToUserNumber(String number);
    void displayFromUserNumber(String number);
    void displayAmount(double amount);
}
