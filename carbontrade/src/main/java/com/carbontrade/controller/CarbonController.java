package com.carbontrade.controller;

import com.carbontrade.model.CarbonFootprint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/carbon")
@CrossOrigin(origins = "*")
public class CarbonController {

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateFootprint(@RequestBody Map<String, Object> request) {
        Double electricity = Double.valueOf(request.get("electricity").toString());
        Double travel = Double.valueOf(request.get("travel").toString());
        Integer dietType = Integer.valueOf(request.get("dietType").toString());
        
        // Create three separate footprint entries for different categories
        CarbonFootprint electricityFootprint = new CarbonFootprint();
        electricityFootprint.setCategory("ELECTRICITY");
        electricityFootprint.setAmount(electricity);
        electricityFootprint.setUnit("kWh");
        electricityFootprint.setEmissionFactor(0.5); // kg CO2 per kWh
        electricityFootprint.setDate(LocalDate.now());
        electricityFootprint.calculateKgCO2e();
        
        CarbonFootprint travelFootprint = new CarbonFootprint();
        travelFootprint.setCategory("TRAVEL");
        travelFootprint.setAmount(travel);
        travelFootprint.setUnit("km");
        travelFootprint.setEmissionFactor(0.2); // kg CO2 per km
        travelFootprint.setDate(LocalDate.now());
        travelFootprint.calculateKgCO2e();
        
        CarbonFootprint dietFootprint = new CarbonFootprint();
        dietFootprint.setCategory("DIET");
        dietFootprint.setAmount(dietType.doubleValue());
        dietFootprint.setUnit("type");
        dietFootprint.setEmissionFactor(100.0); // kg CO2 per type level
        dietFootprint.setDate(LocalDate.now());
        dietFootprint.calculateKgCO2e();
        
        double totalKgCO2e = electricityFootprint.getKgCO2e() + 
                            travelFootprint.getKgCO2e() + 
                            dietFootprint.getKgCO2e();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalCO2Tonnes", Math.round(totalKgCO2e / 1000.0 * 100.0) / 100.0);
        response.put("cerNeeded", Math.ceil(totalKgCO2e / 1000.0));
        response.put("breakdown", Map.of(
            "electricity", Math.round(electricityFootprint.getKgCO2e() / 1000.0 * 100.0) / 100.0,
            "travel", Math.round(travelFootprint.getKgCO2e() / 1000.0 * 100.0) / 100.0,
            "diet", Math.round(dietFootprint.getKgCO2e() / 1000.0 * 100.0) / 100.0
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("message", "Carbon Footprint Calculator API");
        info.put("electricityFactor", "0.5 kg CO2 per kWh");
        info.put("travelFactor", "0.2 kg CO2 per km");
        info.put("dietTypes", "1=Vegan, 2=Vegetarian, 3=Meat-eater");
        return ResponseEntity.ok(info);
    }
}