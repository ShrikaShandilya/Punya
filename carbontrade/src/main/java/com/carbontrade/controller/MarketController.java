package com.carbontrade.controller;

import com.carbontrade.service.MarketService;
import com.carbontrade.service.TransactionService;
import com.carbontrade.service.UserService;
import com.carbontrade.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketController {
    
    @Autowired
    private MarketService marketService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> getCurrentPrice() {
        Double price = marketService.getCurrentPrice();
        Map<String, Object> response = new HashMap<>();
        response.put("pricePerCER", price);
        response.put("currency", "USD");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyCER(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Double amount = Double.valueOf(request.get("amount").toString());
        
        Double price = marketService.getCurrentPrice();
        Double totalCost = amount * price;
        
        // Update user balance
        userService.updateBalance(userId, amount);
        
        // Create transaction record
        Transaction transaction = transactionService.createTransaction(userId, "BUY", amount, price);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("transactionId", transaction.getId());
        response.put("amount", amount);
        response.put("price", price);
        response.put("totalCost", totalCost);
        response.put("message", "CER tokens purchased successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell")
    public ResponseEntity<Map<String, Object>> sellCER(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Double amount = Double.valueOf(request.get("amount").toString());
        
        Map<String, Object> response = new HashMap<>();
        
        if (!userService.hasEnoughBalance(userId, amount)) {
            response.put("success", false);
            response.put("message", "Insufficient CER balance");
            return ResponseEntity.badRequest().body(response);
        }
        
        Double price = marketService.getCurrentPrice();
        Double totalRevenue = amount * price;
        
        // Update user balance
        userService.updateBalance(userId, -amount);
        
        // Create transaction record
        Transaction transaction = transactionService.createTransaction(userId, "SELL", amount, price);
        
        response.put("success", true);
        response.put("transactionId", transaction.getId());
        response.put("amount", amount);
        response.put("price", price);
        response.put("totalRevenue", totalRevenue);
        response.put("message", "CER tokens sold successfully");
        
        return ResponseEntity.ok(response);
    }
}