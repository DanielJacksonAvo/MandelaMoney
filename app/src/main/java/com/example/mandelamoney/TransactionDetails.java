package com.example.mandelamoney;

public class TransactionDetails {
    private String toUser;
    private String fromUser;
    private float amount;
    private String date;
    private String time;

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
