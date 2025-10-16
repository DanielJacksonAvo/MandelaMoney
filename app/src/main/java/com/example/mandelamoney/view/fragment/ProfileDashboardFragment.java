package com.example.mandelamoney.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.DashboardController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IProfileView;

public class ProfileDashboardFragment extends Fragment implements IProfileView {
    View rootView;
    DashboardController controller;

    public ProfileDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rootView = view;
        setController();
        if (!this.getResources().getBoolean(R.bool.is_tablet_landscape)) {
            controller.DashboardProfileController.loadUserToUi();
        }
        connectToUi();

    }

    private void connectToUi() {
        ConstraintLayout btnEdit = rootView.findViewById(R.id.btn_editprofile_profile);
        ConstraintLayout btnDeposit = rootView.findViewById(R.id.btn_deposit_profile);
        ConstraintLayout btnChangePassword = rootView.findViewById(R.id.btn_changepassword_profile);
        ConstraintLayout btnLogout = rootView.findViewById(R.id.btn_logout_profile);
        if (controller != null && controller.DashboardProfileController != null) {
            if (btnEdit != null) {
                btnEdit.setOnClickListener((view) -> controller.DashboardProfileController.handleEditButton());
            }
            if (btnDeposit != null){
                btnDeposit.setOnClickListener((view) -> controller.DashboardProfileController.handleDepositButton());
            }
            if (btnChangePassword != null) {
                btnChangePassword.setOnClickListener((view) -> controller.DashboardProfileController.handleChangePasswordButton());
            }
            if (btnLogout != null) {
                btnLogout.setOnClickListener((view) -> controller.DashboardProfileController.handleLogoutButton());
            }
        }
    }

    public void setController() {
        this.controller = (DashboardController) DataShare.receive();
        if (this.controller != null) {
            this.controller.createDashboardProfileController(this);
        }
    }

    @Override
    public void setFirstNameLabel(String label) {
        TextView textView = rootView.findViewById(R.id.txt_firstnamelabel_profile);
        if (textView != null) {
            textView.setText(label);
        }
    }

    @Override
    public void setFirstName(String firstName) {
        TextView textView = rootView.findViewById(R.id.txt_firstname_profile);
        if (textView != null) {
            textView.setText(firstName);
        }
    }

    @Override
    public void setLastNameLabel(String label) {
        TextView textView = rootView.findViewById(R.id.txt_lastnamelabel_profile);
        if (textView != null) {
            textView.setText(label);
        }
    }

    @Override
    public void setLastName(String lastName) {
        TextView textView = rootView.findViewById(R.id.txt_lastname_profile);
        if (textView != null) {
            textView.setText(lastName);
        }
    }

    @Override
    public void setEmail(String email) {
        TextView textView = rootView.findViewById(R.id.txt_email_profile);
        if (textView != null) {
            textView.setText(email);
        }
    }

    @Override
    public void setStudentNumberLabel(String label) {
        TextView textView = rootView.findViewById(R.id.txt_studentnumberlabel_profile);
        if (textView != null) {
            textView.setText(label);
        }
    }

    @Override
    public void setStudentNumber(String number) {
        TextView textView = rootView.findViewById(R.id.txt_studentnumber_profile);
        if (textView != null) {
            textView.setText(number);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setBalance(Float balance) {
        TextView textView = rootView.findViewById(R.id.txt_balance_profile);
        if (textView != null) {
            textView.setText("R " + balance);
        }
    }

    @Override
    public void setWelcomeName(String name) {
        TextView textView = rootView.findViewById(R.id.txt_user_name_profile);
        if (textView != null) {
            textView.setText(name);
        }
    }

}