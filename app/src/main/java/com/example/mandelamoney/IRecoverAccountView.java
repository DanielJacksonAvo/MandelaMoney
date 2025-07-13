package com.example.mandelamoney;

public interface IRecoverAccountView {
    void showErrorMessage_InvalidCode();
    void hideErrorMessage_InvalidCode();
    void finishActivity();
}
