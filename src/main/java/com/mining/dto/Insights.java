package com.carbontrade.mining.dto;

import java.util.List;

public record Insights(
        ClusterResult clusters,
        List<AnomalyPoint> anomalies
) {}
