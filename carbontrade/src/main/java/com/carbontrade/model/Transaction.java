package com.carbontrade.model;

import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private Long userId;
    private String type; // BUY, SELL, OFFSET
    private Double amount;
    private Double pricePerCer;
    private Double totalValue;
    private LocalDateTime timestamp;

    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(Long userId, String type, Double amount, Double pricePerCer) {
        this();
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.pricePerCer = pricePerCer;
        this.totalValue = amount * pricePerCer;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public Long getUserId() { 
        return userId; 
    }
    
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }
    
    public String getType() { 
        return type; 
    }
    
    public void setType(String type) { 
        this.type = type; 
    }
    
    public Double getAmount() { 
        return amount; 
    }
    
    public void setAmount(Double amount) { 
        this.amount = amount; 
    }
    
    public Double getPricePerCer() { 
        return pricePerCer; 
    }
    
    public void setPricePerCer(Double pricePerCer) { 
        this.pricePerCer = pricePerCer; 
    }
    
    public Double getTotalValue() { 
        return totalValue; 
    }
    
    public void setTotalValue(Double totalValue) { 
        this.totalValue = totalValue; 
    }
    
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }
    
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; 
    }
}