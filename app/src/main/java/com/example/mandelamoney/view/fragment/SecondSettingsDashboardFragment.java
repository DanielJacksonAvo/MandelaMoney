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

public class SecondSettingsDashboardFragment extends Fragment {
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
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        connectToUI();
        if (!this.getResources().getBoolean(R.bool.is_tablet_landscape)) {
            controller.DashboardSettingsController.loadUserToUI();
            controller.DashboardSettingsController.displayNetworkStatus();
            controller.DashboardSettingsController.displayCameraPermission();
        }
        controller.DashboardSettingsController.displayAvailableAuthenticationSettings();
    }

    private void connectToUI() {
        if (this.getResources().getBoolean(R.bool.is_tablet_landscape)) {
            txtUserName = rootView.findViewById(R.id.txt_user_name_settings);
            txtConnectionStatus = rootView.findViewById(R.id.txt_connectionstatus_settings);
            txtConnectionQuality = rootView.findViewById(R.id.txt_connectionquality_settings);
            txtCameraPermission = rootView.findViewById(R.id.txt_camerapermission_settings);
        }

    }
}