package com.example.mandelamoney;

public interface IEnterAmountRequestPaymentView {
    void showError(String message);

    void hideError();

    void finishActivity();
}
