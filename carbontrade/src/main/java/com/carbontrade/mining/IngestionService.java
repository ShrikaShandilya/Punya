package com.carbontrade.mining;

import com.carbontrade.model.CarbonFootprint;
import com.carbontrade.repository.CarbonFootprintRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    private final CarbonFootprintRepository repository;

    public IngestionService(CarbonFootprintRepository repository) {
        this.repository = repository;
    }

    /**
     * Expect CSV columns:
     * userId,date,category,amount,unit,emissionFactor(optional)
     */
    public int importCsv(MultipartFile file) throws Exception {
        List<CarbonFootprint> batch = new ArrayList<>();

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            for (CSVRecord r : parser) {
                Long userId = Long.valueOf(r.get("userId"));
                LocalDate date = LocalDate.parse(r.get("date"));
                String category = r.get("category");
                double amount = Double.parseDouble(r.get("amount"));
                String unit = r.get("unit");

                Double ef = (r.isSet("emissionFactor") && !r.get("emissionFactor").isBlank())
                        ? Double.valueOf(r.get("emissionFactor"))
                        : null;

                if (ef == null) {
                    ef = defaultEmissionFactor(category);
                }

                CarbonFootprint cf = new CarbonFootprint();
                cf.setUserId(userId);
                cf.setDate(date);
                cf.setCategory(category);
                cf.setAmount(amount);
                cf.setUnit(unit);
                cf.setEmissionFactor(ef);
                cf.calculateKgCO2e();

                batch.add(cf);

                if (batch.size() >= 1000) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            repository.saveAll(batch);
        }

        return batch.size(); // Note: This return value is now incorrect as it only counts the last batch. We
                             // should fix it.
    }

    private double defaultEmissionFactor(String category) {
        return switch (category.toLowerCase()) {
            case "electricity" -> 0.417; // kgCO2e per kWh
            case "car" -> 0.192; // kgCO2e per km
            case "flight" -> 0.255; // kgCO2e per km
            default -> 0.0;
        };
    }
}