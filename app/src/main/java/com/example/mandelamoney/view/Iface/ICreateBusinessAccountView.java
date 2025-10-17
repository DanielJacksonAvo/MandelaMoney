package com.example.mandelamoney.view.Iface;

public interface ICreateBusinessAccountView {
    void showEmailError(String error);
    void showBusinessNameError();
    void showPhoneError();
    void showVATError();
    void showPasswordError(String Error, boolean forPassword);
    void hideEmailError();
    void hideBusinessNameError();
    void hidePhoneError();
    void hideVATError();
    void hidePasswordError();

    void finishActivity();
    void showLoadingSpinner();
    void hideLoadingSpinner();
}
