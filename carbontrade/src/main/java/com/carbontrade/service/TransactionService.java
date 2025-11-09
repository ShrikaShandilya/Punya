package com.carbontrade.service;

import com.carbontrade.model.Transaction;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private AtomicLong idCounter = new AtomicLong(1);

    public Transaction createTransaction(Long userId, String type, Double amount, Double price) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setPricePerCer(price);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.calculateTotalValue();
        transaction.setId(idCounter.getAndIncrement());
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactions.values().stream()
            .filter(t -> t.getUserId().equals(userId))
            .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
            .collect(Collectors.toList());
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    public Transaction getTransactionById(Long id) {
        return transactions.get(id);
    }
}