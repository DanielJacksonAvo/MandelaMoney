package com.example.mandelamoney.view.fragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.view.Iface.ISettingsView;

public class SettingsDashboardFragment extends Fragment implements ISettingsView {

    private TextView txtUserName;

    public SettingsDashboardFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_dashboard, container, false);
    }

    private void connectToUI() {
        txtUserName = rootView.findViewById(R.id.txt_user_name_dashboard);
    }

    @Override
    public void displayUserName(String name) {

    }
}