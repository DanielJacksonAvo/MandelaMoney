package com.example.mandelamoney.model;

import java.io.Serializable;

public class Business extends User implements Serializable {
    private String businessName, businessPhoneNumber, businessVAT;

    public Business(String userEmail) {
        super(userEmail);
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
