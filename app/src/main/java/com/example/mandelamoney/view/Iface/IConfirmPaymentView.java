package com.example.mandelamoney.view.Iface;

public interface IConfirmPaymentView {
    void displayToUserName(String name);
    void displayFromUserName(String name);
    void displayToUserNumber(String number);
    void displayFromUserNumber(String number);
    void displayToUserTransactionType(String type);
    void displayFromUserTransactionType(String type);
    void displayAmount(double amount);
    void finishActivity();
    void showLoadingSpinner();
    void hideLoadingSpinner();
}
