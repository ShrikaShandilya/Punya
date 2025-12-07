package com.carbontrade.controller.dto;

public class RulesResponse {
    public int pointsPerCoin;
    public boolean automaticConversion;

    public RulesResponse(int pointsPerCoin, boolean automaticConversion) {
        this.pointsPerCoin = pointsPerCoin;
        this.automaticConversion = automaticConversion;
    }
}
