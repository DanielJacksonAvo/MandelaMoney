package com.example.mandelamoney.model;

import java.util.Objects;
import java.util.UUID;

public class Transaction {
    private String id;
    private String toUser;
    private String fromUser;
    private float amount;
    private final String date;
    private final String time;
    private String displayName;
    private boolean selfTransaction;

    public Transaction(String fromUser, String toUser, float amount, String date, String time) {
        this.id = UUID.randomUUID().toString();
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.displayName ="";
    }

    public String getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Float.compare(that.amount, amount) == 0 &&
                isSelfTransaction() == that.isSelfTransaction() &&
                Objects.equals(toUser, that.toUser) &&
                Objects.equals(fromUser, that.fromUser) &&
                Objects.equals(date, that.date) &&
                Objects.equals(time, that.time) &&
                Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, toUser, fromUser, amount, date, time, displayName, selfTransaction);
    }
}