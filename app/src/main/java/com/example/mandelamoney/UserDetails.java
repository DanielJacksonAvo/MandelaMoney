package com.example.mandelamoney;
public class UserDetails {
    private String email;
    private String firstName;
    private String lastName;
    private String number;     // studentNumber or VAT
    private String userType;   // student or business

    // Getters
    public String getEmail() {
        return email;
    }

    public String getFullName() {
        if (userType.equalsIgnoreCase("student")) {
            return (firstName + " " + lastName).trim();
        } else {
            return firstName; // Business name stored as firstName
        }
    }

    public String getNumber() {
        return number;
    }

    public String getUserType() {
        return userType;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}

