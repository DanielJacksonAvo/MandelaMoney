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
import com.example.mandelamoney.view.Iface.ISettingsView;

public class SettingsDashboardFragment extends Fragment implements ISettingsView {

    private TextView txtUserName, txtConnectionStatus, txtConnectionQuality, txtCameraPermission;
    private Switch swchFaceID, swchFingerprint;
    private View rootView;


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
    }

    private void connectToUI() {
        txtUserName = rootView.findViewById(R.id.txt_user_name_dashboard);
        txtConnectionStatus = rootView.findViewById(R.id.txt_connectionstatus_settings);
        txtConnectionQuality = rootView.findViewById(R.id.txt_connectionquality_settings);
        txtCameraPermission = rootView.findViewById(R.id.txt_camerapermission_settings);
        swchFaceID = rootView.findViewById(R.id.swch_faceid_settings);
        swchFingerprint = rootView.findViewById(R.id.swch_fingerprint_settings);

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
    public void updateFaceIDSwitchFunctionality(Boolean enabled) {
        if (!enabled) {
            setFaceIDSwitchStatus(false);
        }
        swchFaceID.setEnabled(enabled);
    }

    @Override
    public void updateFingerprintSwitchFunctionality(Boolean enabled) {
        if (!enabled) {
            setFingerprintSwitchStatus(false);
        }
        swchFingerprint.setEnabled(enabled);
    }

    @Override
    public void setFaceIDSwitchStatus(Boolean on) {
        swchFaceID.setChecked(on);
    }

    @Override
    public void setFingerprintSwitchStatus(Boolean on) {
        swchFingerprint.setChecked(on);
    }


}