package com.carbontrade.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.carbontrade.model.User;

@Entity
public class Token {
    @Id
    private String token;

    @ManyToOne
    private User user;

    private LocalDateTime expiry;

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }
}