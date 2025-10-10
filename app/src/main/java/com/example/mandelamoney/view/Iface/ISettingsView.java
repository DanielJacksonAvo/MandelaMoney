package com.example.mandelamoney.view.Iface;

public interface ISettingsView {
    void displayUserName(String name);
    void displayConnectionStatus(String status);
    void displayConnectionQuality(String status);
    void displayCameraPermission(String status);
    void updateWeakBiometricsSwitchFunctionality(Boolean enabled);
    void updateBiometricsSwitchFunctionality(Boolean enabled);
    void setWeakBiometricsSwitchStatus(Boolean on);
    void setBiometricsSwitchStatus(Boolean on);

}
