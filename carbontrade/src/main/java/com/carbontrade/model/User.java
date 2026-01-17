package com.carbontrade.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "cer_balance", nullable = false, columnDefinition = "DOUBLE PRECISION")
    private Double cerBalance = 0.0;

    @Column(name = "kyc_status", nullable = false)
    private String kycStatus = "PENDING";

    @Column(name = "role", nullable = false)
    private String role = "USER";

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Maps the user's role to Spring Security's GrantedAuthority.
     */
    public Collection<GrantedAuthority> getAuthorities() {
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    public boolean getActive() {
        return active;
    }
}
