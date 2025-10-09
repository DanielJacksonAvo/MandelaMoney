package com.example.mandelamoney.model;

import java.io.Serializable;

public class User implements Serializable {
    private final String userEmail;
    private String userPassword;
    private float userBalance;
    private boolean strongAuth;
    private boolean weakAuth;

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

    public boolean getStrongAuth() {
        return strongAuth;
    }

    public void setStrongAuth(boolean strongAuth) {
        this.strongAuth = strongAuth;
    }

    public boolean getWeakAuth() {
        return weakAuth;
    }

    public void setWeakAuth(boolean weakAuth) {
        this.weakAuth = weakAuth;
    }


}
