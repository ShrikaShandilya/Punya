package com.carbontrade.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private Double cerBalance;
    private String kycStatus;
    private LocalDateTime createdAt;

    public User() {
        this.cerBalance = 0.0;
        this.kycStatus = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public User(String name, String email, String password) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public Double getCerBalance() { 
        return cerBalance; 
    }
    
    public void setCerBalance(Double cerBalance) { 
        this.cerBalance = cerBalance; 
    }
    
    public String getKycStatus() { 
        return kycStatus; 
    }
    
    public void setKycStatus(String kycStatus) { 
        this.kycStatus = kycStatus; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}