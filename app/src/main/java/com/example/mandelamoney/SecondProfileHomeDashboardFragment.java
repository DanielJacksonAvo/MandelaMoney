package com.example.mandelamoney;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mandelamoney.view.Iface.IProfileView;

public class SecondProfileHomeDashboardFragment extends Fragment implements IProfileView {


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

    @Override
    public void setFirstNameLabel(String label) {

    }

    @Override
    public void setFirstName(String firstName) {

    }

    @Override
    public void setLastNameLabel(String label) {

    }

    @Override
    public void setLastName(String lastName) {

    }

    @Override
    public void setEmail(String email) {

    }

    @Override
    public void setStudentNumberLabel(String label) {

    }

    @Override
    public void setStudentNumber(String number) {

    }

    @Override
    public void setBalance(Float balance) {

    }

    @Override
    public void setWelcomeName(String name) {

    }
}