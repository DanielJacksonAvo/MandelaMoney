package com.example.mandelamoney.view.Iface;

public interface ICreateStudentAccountView {
    void showPasswordError(String message);

    void hidePasswordError();

    void showDetailError(String message);

    void hideDetailError();

    void finishActivity();
}
