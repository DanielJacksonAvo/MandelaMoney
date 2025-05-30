package com.example.mandelamoney;

public class Transaction {
    private String senderEmail, receiverEmail;
    private double amount;
    private int type;

    public Transaction(String senderEmail, String receiverEmail, double amount, int type) {
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.type = type;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
