package com.example.mandelamoney.controller;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.ISettingsView;

public class SettingsController {
    private final ISettingsView view;

    public SettingsController(ISettingsView view) {
        this.view = view;
    }

    public void loadUserToUI() {
        if (UserSession.getUser() instanceof Student) {
            view.displayUserName(((Student) UserSession.getUser()).getStudentFullName());
        } else {
            view.displayUserName(((Business) UserSession.getUser()).getBusinessName());
        }
    }
}
