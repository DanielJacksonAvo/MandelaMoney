package com.example.mandelamoney;

public interface IForgotPasswordView {
    void showErrorMessage_InvalidEmail();
    void hideErrorMessage_InvalidEmail();
    void finishActivity();
}
