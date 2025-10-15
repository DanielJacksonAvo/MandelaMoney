package com.example.mandelamoney.view.Iface;

public interface IForgotPasswordView {
    void showErrorMessage_InvalidEmail();
    void hideErrorMessage_InvalidEmail();
    void showLoadingSpinner();
    void hideLoadingSpinner();
    void finishActivity();
}
