package com.example.mandelamoney.view.Iface;

public interface ILoginView {
    void showErrorMessage();

    void hideErrorMessage();
    void showPasswordError();
    void hidePasswordError();
    void showEmailErrorMessage(String error);
    void hideEmailErrorMessage();

    void finishActivity();

    void hideLoadingSpinner();

    void showLoadingSpinner();
}
