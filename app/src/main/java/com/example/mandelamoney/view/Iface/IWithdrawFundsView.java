package com.example.mandelamoney.view.Iface;

public interface IWithdrawFundsView {
    void showMissingFieldError(String message);
    void hideMissingFieldError();
    void showInvalidFieldError(String message);
    void hideInvalidFieldError();
    void finishActivity();
}
