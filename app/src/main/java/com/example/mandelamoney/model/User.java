package com.example.mandelamoney.model;

import java.io.Serializable;

public class User implements Serializable {
    private final String userEmail;
    private String userPassword;
    private float userBalance;

    public User(String userEmail) {
       this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void setUserBalance(float balance) {
        userBalance = balance;
    }
    public float getUserBalance() {
        return userBalance;
    }


}
