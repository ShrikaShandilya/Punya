package com.carbontrade.service;

import com.carbontrade.model.User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private Map<Long, User> users = new ConcurrentHashMap<>();
    private AtomicLong idCounter = new AtomicLong(1);

    public User registerUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setCerBalance(0.0);
        user.setKycStatus("PENDING");
        user.setCreatedAt(LocalDateTime.now());
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    public User loginUser(String email, String password) {
        return users.values().stream()
            .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null);
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public User updateBalance(Long userId, Double amount) {
        User user = users.get(userId);
        if (user != null) {
            user.setCerBalance(user.getCerBalance() + amount);
        }
        return user;
    }

    public boolean hasEnoughBalance(Long userId, Double amount) {
        User user = users.get(userId);
        return user != null && user.getCerBalance() >= amount;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}