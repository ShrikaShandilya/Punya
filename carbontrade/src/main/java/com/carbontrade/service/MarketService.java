package com.carbontrade.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class MarketService {
    private Double currentPrice = 25.50;
    private Random random = new Random();

    public Double getCurrentPrice() {
        // Simulate realistic price fluctuation
        Double change = (random.nextDouble() - 0.5) * 2;
        currentPrice = Math.max(20.0, Math.min(30.0, currentPrice + change));
        return Math.round(currentPrice * 100.0) / 100.0;
    }

    public void setCurrentPrice(Double price) {
        this.currentPrice = price;
    }

    public Double calculateTotalCost(Double amount) {
        return amount * getCurrentPrice();
    }
}