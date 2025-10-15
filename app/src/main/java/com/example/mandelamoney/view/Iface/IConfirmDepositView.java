package com.example.mandelamoney.view.Iface;

public interface IConfirmDepositView {
    void displayToUserName(String name);
    void displayFromUserName(String name);
    void displayToUserNumber(String number);
    void displayFromUserNumber(String number);
    void displayAmount(double amount);
    void finishActivity();

    void showLoadingSpinner();

    void hideLoadingSpinner();
}
