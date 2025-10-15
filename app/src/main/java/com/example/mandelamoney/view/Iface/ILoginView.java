package com.example.mandelamoney.view.Iface;

public interface ILoginView {
    void showErrorMessage();

    void hideErrorMessage();
    void showEmailErrorMessage();
    void hideEmailErrorMessage();

    void finishActivity();

    void hideLoadingSpinner();

    void showLoadingSpinner();
}
