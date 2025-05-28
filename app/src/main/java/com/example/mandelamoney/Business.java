package com.example.mandelamoney;

public class Business extends User {
    private String businessName, businessPhoneNumber, businessVAT;

    public Business(String userEmail, String userPassword, double userBalance, int userType, String businessName, String businessPhoneNumber, String businessVAT) {
        super(userEmail, userPassword, userBalance, userType);
        this.businessName = businessName;
        this.businessPhoneNumber = businessPhoneNumber;
        this.businessVAT = businessVAT;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessPhoneNumber() {
        return businessPhoneNumber;
    }

    public void setBusinessPhoneNumber(String businessPhoneNumber) {
        this.businessPhoneNumber = businessPhoneNumber;
    }

    public String getBusinessVAT() {
        return businessVAT;
    }

    public void setBusinessVAT(String businessVAT) {
        this.businessVAT = businessVAT;
    }
}
