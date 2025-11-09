package com.carbontrade.mining.dto;

import java.time.LocalDate;

public class AnomalyPoint {

    private String date;
    private double score;

    public AnomalyPoint() {}

    // Constructor used by MiningService
    public AnomalyPoint(LocalDate date, double score) {
        this.date = date.toString();
        this.score = score;
    }

    // Not required but kept for compatibility
    public AnomalyPoint(String timestamp, double value, double score) {
        this.date = timestamp;
        this.score = score;
    }

    public String getDate() { return date; }
    public double getScore() { return score; }

    // Required by MiningService
    public double score() {
        return score;
    }
}
