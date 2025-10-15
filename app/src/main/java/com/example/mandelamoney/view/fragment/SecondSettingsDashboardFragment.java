package com.example.mandelamoney.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.ISettingsView;

public class SecondSettingsDashboardFragment extends Fragment implements ISettingsView {
    private View rootView;
    private DashboardController controller;
    private TextView txtUserName, txtConnectionStatus, txtConnectionQuality, txtCameraPermission;



    public SecondSettingsDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second_settings_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rootView = view;
        controller.DashboardSettingsController.setTabletView(this);
        connectToUI();
        controller.DashboardSettingsController.loadUserToUI();
        controller.DashboardSettingsController.displayNetworkStatus();
        controller.DashboardSettingsController.displayCameraPermission();
    }

    private void connectToUI() {
        if (this.getResources().getBoolean(R.bool.is_tablet_landscape)) {
            txtConnectionStatus = rootView.findViewById(R.id.txt_connectionstatus_settings);
            txtConnectionQuality = rootView.findViewById(R.id.txt_connectionquality_settings);
            txtCameraPermission = rootView.findViewById(R.id.txt_camerapermission_settings);
        }

    }

    public void setController(DashboardController controller) {
        this.controller = controller;
        if (this.controller != null && this.controller.DashboardSettingsController == null) {
            this.controller.createDashboardSettingsController(this);
        }
    }

    @Override
    public void displayUserName(String name) {

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

    }

    @Override
    public void updateBiometricsSwitchFunctionality(Boolean available) {

    }

    @Override
    public void setWeakBiometricsSwitchStatus(Boolean on) {

    }

    @Override
    public void setBiometricsSwitchStatus(Boolean on) {

    }
}