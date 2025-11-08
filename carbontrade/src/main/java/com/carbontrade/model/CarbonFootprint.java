package com.carbontrade.model;

public class CarbonFootprint {
    private Double electricity; // kWh per month
    private Double travel; // km per month
    private Integer dietType; // 1=Vegan, 2=Vegetarian, 3=Meat-eater
    private Double totalCO2; // tonnes

    public CarbonFootprint() {}

    public CarbonFootprint(Double electricity, Double travel, Integer dietType) {
        this.electricity = electricity;
        this.travel = travel;
        this.dietType = dietType;
        this.totalCO2 = calculateTotal();
    }

    private Double calculateTotal() {
        // Electricity: 0.5 kg CO2 per kWh = 0.0005 tonnes per kWh
        Double electricityCO2 = electricity * 0.0005;
        
        // Travel: 0.2 kg CO2 per km = 0.0002 tonnes per km
        Double travelCO2 = travel * 0.0002;
        
        // Diet: base 0.1 tonnes per type level
        Double dietCO2 = dietType * 0.1;
        
        return electricityCO2 + travelCO2 + dietCO2;
    }

    // Getters and Setters
    public Double getElectricity() { 
        return electricity; 
    }
    
    public void setElectricity(Double electricity) { 
        this.electricity = electricity;
        this.totalCO2 = calculateTotal();
    }
    
    public Double getTravel() { 
        return travel; 
    }
    
    public void setTravel(Double travel) { 
        this.travel = travel;
        this.totalCO2 = calculateTotal();
    }
    
    public Integer getDietType() { 
        return dietType; 
    }
    
    public void setDietType(Integer dietType) { 
        this.dietType = dietType;
        this.totalCO2 = calculateTotal();
    }
    
    public Double getTotalCO2() { 
        return totalCO2; 
    }
    
    public void setTotalCO2(Double totalCO2) { 
        this.totalCO2 = totalCO2; 
    }
}