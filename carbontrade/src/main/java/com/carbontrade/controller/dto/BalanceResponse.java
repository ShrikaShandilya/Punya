package com.carbontrade.controller.dto;

public class BalanceResponse {
    public Long userId;
    public long points;
    public long coins;

    public BalanceResponse(Long userId, long points, long coins) {
        this.userId = userId;
        this.points = points;
        this.coins = coins;
    }
}
