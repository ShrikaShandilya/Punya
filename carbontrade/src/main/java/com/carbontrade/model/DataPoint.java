package com.carbontrade.model;

public class DataPoint {

    private Long userId;
    private String date;
    private double amount;

    public DataPoint() {}

    public DataPoint(Long userId, String date, double amount) {
        this.userId = userId;
        this.date = date;
        this.amount = amount;
    }

    public Long getUserId() { return userId; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setDate(String date) { this.date = date; }
    public void setAmount(double amount) { this.amount = amount; }
}
