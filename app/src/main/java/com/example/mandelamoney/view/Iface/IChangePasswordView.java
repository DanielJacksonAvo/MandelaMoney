package com.example.mandelamoney.view.Iface;

public interface IChangePasswordView {
   void showCurrentPasswordError(String s);
   void hideCurrentPasswordError();
   void showNewPasswordError(String s);
   void hideNewPasswordError();

    void finishActivity();

    void hideLoadingSpinner();
    void showLoadingSpinner();
}
