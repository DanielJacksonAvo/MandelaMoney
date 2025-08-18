package com.example.mandelamoney.view.Iface;

import com.example.mandelamoney.controller.DashboardController;

public interface IProfileView {
    void setFirstNameLabel(String label);
    void setFirstName(String firstName);
    void setLastNameLabel(String label);
    void setLastName(String lastName);
    void setEmail(String email);
    void setStudentNumberLabel(String label);
    void setStudentNumber(String number);
    void setBalance(Float balance);
    void setWelcomeName(String name);


}
