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
import com.example.mandelamoney.view.Iface.IProfileView;

public class SecondProfileHomeDashboardFragment extends Fragment implements IProfileView {

    View rootView;
    DashboardController controller;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rootView = view;
        setController();
        controller.DashboardProfileController.loadUserToUi();
    }




    public SecondProfileHomeDashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second_profile_home_dashboard, container, false);
    }

    public void setController() {
        this.controller = (DashboardController) DataShare.receive();
        if (this.controller != null) {
            this.controller.createDashboardProfileController(this);
        }
        DataShare.send(controller);
    }

    @Override
    public void setFirstNameLabel(String label) {
        TextView textView = rootView.findViewById(R.id.txt_firstnamelabel_profile);
        textView.setText(label);
    }

    @Override
    public void setFirstName(String firstName) {
        TextView textView = rootView.findViewById(R.id.txt_firstname_profile);
        textView.setText(firstName);
    }

    @Override
    public void setLastNameLabel(String label) {
        TextView textView = rootView.findViewById(R.id.txt_lastnamelabel_profile);
        textView.setText(label);
    }

    @Override
    public void setLastName(String lastName) {
        TextView textView = rootView.findViewById(R.id.txt_lastname_profile);
        textView.setText(lastName);
    }

    @Override
    public void setEmail(String email) {
        TextView textView = rootView.findViewById(R.id.txt_email_profile);
        textView.setText(email);
    }

    @Override
    public void setStudentNumberLabel(String label) {
        TextView textView = rootView.findViewById(R.id.txt_studentnumberlabel_profile);
        textView.setText(label);
    }

    @Override
    public void setStudentNumber(String number) {
        TextView textView = rootView.findViewById(R.id.txt_studentnumber_profile);
        textView.setText(number);
    }

    @Override
    public void setBalance(Float balance) {
        TextView textView = rootView.findViewById(R.id.txt_balance_profile);
        textView.setText("R " + Float.toString(balance));
    }

    @Override
    public void setWelcomeName(String name) {
        TextView textView = rootView.findViewById(R.id.txt_user_name_profile);
        textView.setText(name);
    }
}