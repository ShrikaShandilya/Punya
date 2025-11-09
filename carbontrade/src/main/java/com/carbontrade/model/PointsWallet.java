package com.carbontrade.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "points_wallet", indexes = {
        @Index(name = "idx_points_wallet_user", columnList = "userId", unique = true)
})
public class PointsWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    // Current, spendable balances
    private long points;   // accumulated points (auto-convert trims this below 50)
    private long coins;    // minted coins

    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate
    public void touch() {
        updatedAt = OffsetDateTime.now();
    }

    public PointsWallet() {}
    public PointsWallet(Long userId) {
        this.userId = userId;
    }

    // getters & setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public long getPoints() { return points; }
    public void setPoints(long points) { this.points = points; }
    public long getCoins() { return coins; }
    public void setCoins(long coins) { this.coins = coins; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
