package com.example.mandelamoney.view.Iface;

public interface ICreateStudentAccountView {
    void finishActivity();
    void showEmailError(String error);
    void showFirstNameError();
    void showLastNameError();
    void showStudentNumberError();
    void showPasswordError(String Error, boolean forPassword);
    void hideEmailError();
    void hideFirstNameError();
    void hideLastNameError();
    void hideStudentNumberError();
    void hidePasswordError();
    void showLoadingSpinner();
    void hideLoadingSpinner();

}
