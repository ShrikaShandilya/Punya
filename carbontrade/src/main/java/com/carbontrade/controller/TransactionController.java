// ============================================
// CARBONTRADE PROJECT - FILE 12 of 13
// ============================================
// CURRENT DIRECTORY STRUCTURE:
// carbontrade/
// ├── pom.xml  ✓
// └── src/
//     └── main/
//         ├── resources/
//         │   └── application.properties  ✓
//         └── java/
//             └── com/
//                 └── carbontrade/
//                     ├── CarbonTradeApplication.java  ✓
//                     ├── model/
//                     │   ├── User.java  ✓
//                     │   ├── Transaction.java  ✓
//                     │   └── CarbonFootprint.java  ✓
//                     ├── service/
//                     │   ├── UserService.java  ✓
//                     │   ├── TransactionService.java  ✓
//                     │   └── MarketService.java  ✓
//                     └── controller/
//                         ├── UserController.java  ✓
//                         ├── MarketController.java  ✓
//                         └── TransactionController.java  <-- YOU ARE HERE
//
// ============================================

package com.carbontrade.controller;

import com.carbontrade.model.Transaction;
import com.carbontrade.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction != null) {
            return ResponseEntity.ok(transaction);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
}