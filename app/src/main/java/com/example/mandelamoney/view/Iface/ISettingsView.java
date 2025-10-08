package com.example.mandelamoney.view.Iface;

public interface ISettingsView {
    void displayUserName(String name);
    void displayConnectionStatus(String status);
    void displayConnectionQuality(String status);
    void displayCameraPermission(String status);
    void updateFaceIDSwitchFunctionality(Boolean enabled);
    void updateFingerprintSwitchFunctionality(Boolean enabled);
    void setFaceIDSwitchStatus(Boolean on);
    void setFingerprintSwitchStatus(Boolean on);

}
