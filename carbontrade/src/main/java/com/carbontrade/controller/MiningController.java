package com.carbontrade.controller;

import com.carbontrade.mining.IngestionService;
import com.carbontrade.mining.MiningService;
import com.carbontrade.mining.dto.Insights;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mining")
@CrossOrigin(origins = "*")
public class MiningController {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private MiningService miningService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Mining subsystem operational");
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestCarbonData(
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        if (file == null || file.isEmpty()) {
            response.put("success", false);
            response.put("error", "File parameter is required");
            return ResponseEntity.badRequest().body(response);
        }

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

    // NEW ENDPOINT
    @GetMapping("/analyze")
    public java.util.concurrent.CompletableFuture<ResponseEntity<Insights>> analyze(
            @RequestParam Long userId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "4") int k) {
        return miningService.analyze(
                userId,
                LocalDate.parse(start),
                LocalDate.parse(end),
                k).thenApply(ResponseEntity::ok);
    }
}
