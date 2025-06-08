package com.example.mandelamoney;

public interface IResetPasswordView {
    void showErrorMessage_PasswordsDoNotMatch();
    void hideErrorMessage_PasswordsDoNotMatch();
    void showErrorMessage_Minimum8Characters();
    void hideErrorMessage_Minimum8Characters();
    void finishActivity();

}
