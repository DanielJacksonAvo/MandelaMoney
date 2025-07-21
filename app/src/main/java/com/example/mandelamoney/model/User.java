package com.example.mandelamoney.model;

import static java.lang.Math.abs;
import java.io.Serializable;

public abstract class User implements Serializable {
    private final String userEmail;
    private String userPassword;
    private double userBalance;
    public User(String userEmail, String userPassword, double userBalance) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userBalance = userBalance;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public double getUserBalance() {
        return userBalance;
    }


    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void decreaseBalance(double amount) {
        double newUserBalance = userBalance - abs(amount);
        if (newUserBalance >= 0) {
            userBalance = newUserBalance;
        } else {
            throw new RuntimeException("Invalid Entry: User balance can't be negative");
        }
    }

    public void increaseBalance(double amount) {
        userBalance = userBalance + abs(amount);
    }

    public void setUserBalance(double balance) {
        userBalance = balance;
    }

}
