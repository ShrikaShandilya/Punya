package com.carbontrade.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "points_txn", indexes = @Index(name = "idx_points_txn_user", columnList = "userId"))
public class PointsTransaction {

    public enum Kind { EARN, AUTO_CONVERT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private Kind kind;

    private long deltaPoints; // +N for earn, -50*k for convert
    private long deltaCoins;  // +k when auto convert occurs

    private String reason;    // free-form context (e.g., "mining_analyze", "ingest_csv")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public PointsTransaction() {}

    public PointsTransaction(Long userId, Kind kind, long deltaPoints, long deltaCoins, String reason) {
        this.userId = userId;
        this.kind = kind;
        this.deltaPoints = deltaPoints;
        this.deltaCoins = deltaCoins;
        this.reason = reason;
    }

    // getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Kind getKind() { return kind; }
    public long getDeltaPoints() { return deltaPoints; }
    public long getDeltaCoins() { return deltaCoins; }
    public String getReason() { return reason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
