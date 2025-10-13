package com.example.mandelamoney.view.Iface;

public interface ITransactionStatusDisplayView {
    void displayToUserName(String name);
    void displayFromUserName(String name);
    void displayToUserNumber(String number);
    void displayFromUserNumber(String number);
    void displayAmount(double amount);
    void finishActivity();
    void setFromUserLabelAsBank();
    void setToUserLabelAsBank();

    void setToUserLabelAsMandelaMoney();

    void setFromUserLabelAsMandelaMoney();
}
