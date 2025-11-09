package com.carbontrade.repository;

import com.carbontrade.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions by a specific user (buyer or seller).
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Find all transactions within a date range.
     */
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Return all transactions for a user within a date range.
     */
    List<Transaction> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Sum CO2 credits purchased or sold by a user.
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId")
    Double sumAmountByUser(Long userId);

    /**
     * Group-by aggregation which is useful for dashboards or market analysis.
     */
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t GROUP BY t.type")
    List<Object[]> totalAmountByType();
}
