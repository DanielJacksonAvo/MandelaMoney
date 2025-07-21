package com.example.mandelamoney.model;

public class TransactionDetails {
    private final String toUser;
    private final String fromUser;
    private final float amount;
    private final String date;
    private final String time;

    // Constructor
    public TransactionDetails(String toUser, String fromUser, float amount, String date, String time) {
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.amount = amount;
        this.date = date;
        this.time = time;
    }

    // Getters
    public String getToUser() { return toUser; }
    public String getFromUser() { return fromUser; }
    public float getAmount() { return amount; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}
