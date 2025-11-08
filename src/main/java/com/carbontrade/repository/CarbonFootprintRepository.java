package com.carbontrade.repository;

import com.carbontrade.model.CarbonFootprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CarbonFootprintRepository extends JpaRepository<CarbonFootprint, Long> {

    /**
     * All daily footprints for a user in a date range.
     */
    List<CarbonFootprint> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    /**
     * All footprint records for a user.
     */
    List<CarbonFootprint> findByUserId(Long userId);

    /**
     * Summed emissions by category within a time window.
     * Used in:
     *  - attribution endpoint (/mining/drivers)
     *  - mining module decomposition
     */
    @Query("""
        SELECT cf.category, SUM(cf.kgCO2e)
        FROM CarbonFootprint cf
        WHERE cf.userId = :userId
          AND cf.date BETWEEN :start AND :end
        GROUP BY cf.category
    """)
    List<Object[]> sumByCategory(Long userId, LocalDate start, LocalDate end);

    /**
     * Daily aggregated emissions. Useful for forecasting or 
     * building training sets for ML models.
     */
    @Query("""
        SELECT cf.date, SUM(cf.kgCO2e)
        FROM CarbonFootprint cf
        WHERE cf.userId = :userId
          AND cf.date BETWEEN :start AND :end
        GROUP BY cf.date
        ORDER BY cf.date
    """)
    List<Object[]> dailyTotals(Long userId, LocalDate start, LocalDate end);

    /**
     * Optional: Summaries by month — nice for UI graphs.
     */
    @Query("""
        SELECT EXTRACT(YEAR FROM cf.date), EXTRACT(MONTH FROM cf.date), SUM(cf.kgCO2e)
        FROM CarbonFootprint cf
        WHERE cf.userId = :userId
        GROUP BY EXTRACT(YEAR FROM cf.date), EXTRACT(MONTH FROM cf.date)
        ORDER BY EXTRACT(YEAR FROM cf.date), EXTRACT(MONTH FROM cf.date)
    """)
    List<Object[]> monthlyTotals(Long userId);
}
