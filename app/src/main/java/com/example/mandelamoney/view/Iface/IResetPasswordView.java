package com.example.mandelamoney.view.Iface;

public interface IResetPasswordView {
    void showErrorMessage_PasswordsDoNotMatch(String string);
    void hideErrorMessage_PasswordsDoNotMatch();
    void showErrorMessage_Minimum8Characters(String string);
    void hideErrorMessage_Minimum8Characters();

    void showLoadingSpinner();

    void hideLoadingSpinner();

    void finishActivity();

}
