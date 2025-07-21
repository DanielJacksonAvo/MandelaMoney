package com.example.mandelamoney.view.Iface;

public interface ICreateBusinessAccountView {
    void showPasswordError(String message);

    void hidePasswordError();

    void showDetailError(String message);

    void hideDetailError();

    void finishActivity();
}
