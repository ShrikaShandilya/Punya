package com.carbontrade.service;

import com.carbontrade.model.User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {
    private final com.carbontrade.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserService(com.carbontrade.repository.UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCerBalance(0.0);
        user.setKycStatus("PENDING");
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateBalance(Long userId, Double amount) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setCerBalance(user.getCerBalance() + amount);
            userRepository.save(user);
        }
        return user;
    }

    public boolean hasEnoughBalance(Long userId, Double amount) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getCerBalance() >= amount;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}