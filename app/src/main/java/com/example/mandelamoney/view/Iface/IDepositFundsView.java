package com.example.mandelamoney.view.Iface;

public interface IDepositFundsView {
    void showMissingFieldError(String message);
    void hideMissingFieldError();
    void showInvalidFieldError(String message);
    void finishActivity();

    void hideInvalidFieldError();
}
