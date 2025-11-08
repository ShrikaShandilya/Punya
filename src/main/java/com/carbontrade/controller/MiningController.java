package com.carbontrade.controller;

import com.carbontrade.mining.IngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mining")
@CrossOrigin(origins = "*")
public class MiningController {

    @Autowired
    private IngestionService ingestionService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Mining subsystem operational");
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestCarbonData(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int recordsProcessed = ingestionService.importCsv(file);
            response.put("success", true);
            response.put("recordsProcessed", recordsProcessed);
            response.put("message", "Successfully processed " + recordsProcessed + " records");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
