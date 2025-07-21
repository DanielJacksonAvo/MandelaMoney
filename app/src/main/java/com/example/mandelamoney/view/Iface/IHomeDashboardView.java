package com.example.mandelamoney.view.Iface;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mandelamoney.controller.DashboardController;

public interface IHomeDashboardView {
    void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState);

    void displayBalance(double balance);

    void displayUserName(String name);
}
