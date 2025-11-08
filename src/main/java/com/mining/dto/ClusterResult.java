package com.carbontrade.mining.dto;

public record ClusterResult(
        int k,
        int[] labels,
        double[][] centroids
) {}
