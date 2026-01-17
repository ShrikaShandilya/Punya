package com.carbontrade.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CarbonFootprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private LocalDate date;
    private String category;
    private double amount;
    private String unit;
    private Double emissionFactor;
    private double kgCO2e;

    public void calculateKgCO2e() {
        if (emissionFactor != null) {
            this.kgCO2e = amount * emissionFactor;
        }
    }
}