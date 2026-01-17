package com.carbontrade.mining;

import com.carbontrade.model.CarbonFootprint;
import com.carbontrade.repository.CarbonFootprintRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeatureEngineeringService {

    private final CarbonFootprintRepository repository;

    public FeatureEngineeringService(CarbonFootprintRepository repository) {
        this.repository = repository;
    }

    public record FeatureRow(Long userId, LocalDate date, double[] features, double target) {
    }

    /**
     * Produces one feature vector per day between start and end date.
     */
    public List<FeatureRow> buildDailyFeatures(Long userId, LocalDate start, LocalDate end) {

        List<CarbonFootprint> rows = repository.findByUserIdAndDateBetween(userId, start, end);

        // Unique categories -> one-hot vector space
        List<String> categories = rows.stream()
                .map(CarbonFootprint::getCategory)
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();

        Map<String, Integer> catIndex = new HashMap<>();
        for (int i = 0; i < categories.size(); i++) {
            catIndex.put(categories.get(i), i);
        }

        // Aggregate per day
        Map<LocalDate, double[]> dayCatTotals = new HashMap<>();
        Map<LocalDate, Double> dayTotals = new HashMap<>();

        for (CarbonFootprint cf : rows) {
            LocalDate d = cf.getDate();
            dayCatTotals.computeIfAbsent(d, k -> new double[categories.size()]);
            int idx = catIndex.get(cf.getCategory().toLowerCase());
            dayCatTotals.get(d)[idx] += cf.getKgCO2e();
            dayTotals.merge(d, cf.getKgCO2e(), Double::sum);
        }

        // Monthly trend feature
        Map<YearMonth, Double> monthlyTotals = rows.stream().collect(
                Collectors.groupingBy(cf -> YearMonth.from(cf.getDate()),
                        Collectors.summingDouble(CarbonFootprint::getKgCO2e)));

        List<FeatureRow> featureRows = new ArrayList<>();

        LocalDate pointer = start;
        while (!pointer.isAfter(end)) {

            double todaySum = dayTotals.getOrDefault(pointer, 0.0);
            double[] categoryVector = dayCatTotals.getOrDefault(pointer, new double[categories.size()]);

            double weekday = pointer.getDayOfWeek().getValue(); // 1..7
            double month = pointer.getMonthValue(); // 1..12
            double monthTotal = monthlyTotals.getOrDefault(YearMonth.from(pointer), 0.0);

            double[] vector = new double[4 + categoryVector.length];
            vector[0] = monthTotal;
            vector[1] = weekday;
            vector[2] = month;
            vector[3] = todaySum;

            System.arraycopy(categoryVector, 0, vector, 4, categoryVector.length);

            featureRows.add(new FeatureRow(userId, pointer, vector, todaySum));
            pointer = pointer.plusDays(1);
        }

        return featureRows;
    }
}
