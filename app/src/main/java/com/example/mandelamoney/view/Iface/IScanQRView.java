package com.example.mandelamoney.view.Iface;

public interface IScanQRView {
    void showToast(String message);
    void finishActivity();
    void showLoadingSpinner();
    void hideLoadingSpinner();
}
