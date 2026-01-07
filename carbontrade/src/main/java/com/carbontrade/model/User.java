package com.carbontrade.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    private Double cerBalance = 0.0;
    
    private String kycStatus = "PENDING";
    
    private String role = "USER";
    
    private boolean active = true;
    
    private LocalDateTime createdAt = LocalDateTime.now();
}