package com.example.mandelamoney.model;

public class Transaction {
    private String toUser;
    private String fromUser;
    private float amount;
    private final String date;
    private final String time;
    private String displayName;
    private boolean selfTransaction;

    public Transaction(String fromUser, String toUser, float amount, String date, String time) {
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.displayName ="";
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSelfTransaction() {
        return selfTransaction;
    }

    public void setSelfTransaction(boolean selfTransaction) {
        this.selfTransaction = selfTransaction;
    }
}
