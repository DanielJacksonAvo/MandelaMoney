package com.example.mandelamoney.view.Iface;

public interface IUnlockView {
    void showErrorMessage();
    void hideErrorMessage();
    void finishActivity();

    void hideLoadingSpinner();
    void showLoadingSpinner();
}
