package com.example.mandelamoney.view.Iface;

import android.graphics.Bitmap;

public interface IShowQRCodeRequestPaymentView {
    void displayToast(String message);
    void displayQR(Bitmap bitmap);
    void finishActivity();
}
