package com.carbontrade.controller;

import com.carbontrade.model.CarbonFootprint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        
        CarbonFootprint footprint = new CarbonFootprint(electricity, travel, dietType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalCO2Tonnes", Math.round(footprint.getTotalCO2() * 100.0) / 100.0);
        response.put("cerNeeded", Math.ceil(footprint.getTotalCO2()));
        response.put("breakdown", Map.of(
            "electricity", Math.round(electricity * 0.0005 * 100.0) / 100.0,
            "travel", Math.round(travel * 0.0002 * 100.0) / 100.0,
            "diet", Math.round(dietType * 0.1 * 100.0) / 100.0
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