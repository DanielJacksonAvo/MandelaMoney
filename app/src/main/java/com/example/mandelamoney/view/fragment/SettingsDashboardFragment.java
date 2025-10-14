package com.example.mandelamoney.view.fragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.view.Iface.ISettingsView;

public class SettingsDashboardFragment extends Fragment implements ISettingsView {

    private TextView txtUserName, txtConnectionStatus, txtConnectionQuality, txtCameraPermission;
    private Switch swchFaceID, swchFingerprint;
    private View rootView;
    private DashboardController controller;


    public SettingsDashboardFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        connectToUI();
        configureFaceIDSwitch();
        configureFingerprintSwitch();
        controller.DashboardSettingsController.loadUserToUI();
        controller.DashboardSettingsController.displayNetworkStatus();
        controller.DashboardSettingsController.displayCameraPermission();
        controller.DashboardSettingsController.displayAvailableAuthenticationSettings();
    }

    public void setController(DashboardController controller) {
        this.controller = controller;
        if (this.controller != null) {
            this.controller.createDashboardSettingsController(this);
        }
    }

    private void connectToUI() {
        txtUserName = rootView.findViewById(R.id.txt_user_name_settings);
        txtConnectionStatus = rootView.findViewById(R.id.txt_connectionstatus_settings);
        txtConnectionQuality = rootView.findViewById(R.id.txt_connectionquality_settings);
        txtCameraPermission = rootView.findViewById(R.id.txt_camerapermission_settings);
        swchFaceID = rootView.findViewById(R.id.swch_faceid_settings);
        swchFingerprint = rootView.findViewById(R.id.swch_fingerprint_settings);

    }

    private void configureFaceIDSwitch() {
        swchFaceID.setOnCheckedChangeListener((buttonView, isChecked) -> {
            controller.DashboardSettingsController.handleWeakAuthenticationChange(isChecked);
        });
    }

    private void configureFingerprintSwitch() {
        swchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            controller.DashboardSettingsController.handleStrongAuthenticationChange(isChecked);
        });
    }

    @Override
    public void displayUserName(String name) {
        txtUserName.setText(name);
    }

    @Override
    public void displayConnectionStatus(String status) {
        txtConnectionStatus.setText(status);
    }

    @Override
    public void displayConnectionQuality(String status) {
        txtConnectionQuality.setText(status);
    }

    @Override
    public void displayCameraPermission(String status) {
        txtCameraPermission.setText(status);
    }

    @Override
    public void updateWeakBiometricsSwitchFunctionality(Boolean available) {
        //enable or disable the switch
        swchFaceID.setEnabled(available);
    }

    @Override
    public void updateBiometricsSwitchFunctionality(Boolean available) {
        //enable or disable the switch
        if (!available) {
            updateWeakBiometricsSwitchFunctionality(false);
        }
        swchFingerprint.setEnabled(available);
    }

    @Override
    public void setWeakBiometricsSwitchStatus(Boolean on) {
        //turn the switch on or off
        swchFaceID.setChecked(on);
    }

    @Override
    public void setBiometricsSwitchStatus(Boolean on) {
        //turn the switch on or off
        swchFingerprint.setChecked(on);
    }


}