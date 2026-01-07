package com.carbontrade.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String type; // BUY, SELL, OFFSET
    private Double amount;
    private Double pricePerCer;
    private Double totalValue;
    private LocalDateTime timestamp;

    public void calculateTotalValue() {
        this.totalValue = this.amount * this.pricePerCer;
    }
}