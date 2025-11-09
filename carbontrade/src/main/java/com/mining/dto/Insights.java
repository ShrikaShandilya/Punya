package com.carbontrade.mining.dto;

import java.util.List;

public class Insights {

    private ClusterResult clusters;
    private List<AnomalyPoint> anomalies;
    private String summary;
    private int totalRecords;
    private int totalClusters;
    private int totalAnomalies;

    public Insights() {}

    public Insights(ClusterResult clusters, List<AnomalyPoint> anomalies, int totalRecords) {
        this.clusters = clusters;
        this.anomalies = anomalies;
        this.totalRecords = totalRecords;
        this.totalClusters = (clusters != null ? clusters.getK() : 0);
        this.totalAnomalies = anomalies != null ? anomalies.size() : 0;

        // --- Generate simple automatic summary ---
        this.summary = generateSummary();
    }

    private String generateSummary() {
        if (totalRecords == 0) {
            return "No records found for the requested date range.";
        }

        return String.format(
            "Analyzed %d records across %d behavioral patterns and found %d unusual activity periods.",
            totalRecords, totalClusters, totalAnomalies
        );
    }

    // --- Getters / Setters ---
    public ClusterResult getClusters() { return clusters; }
    public void setClusters(ClusterResult clusters) { this.clusters = clusters; }

    public List<AnomalyPoint> getAnomalies() { return anomalies; }
    public void setAnomalies(List<AnomalyPoint> anomalies) { this.anomalies = anomalies; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getTotalClusters() { return totalClusters; }
    public int getTotalAnomalies() { return totalAnomalies; }
}
