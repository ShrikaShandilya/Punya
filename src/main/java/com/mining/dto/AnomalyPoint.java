package com.carbontrade.mining.dto;

import java.time.LocalDate;

public record AnomalyPoint(
        LocalDate date,
        double score
) {}
