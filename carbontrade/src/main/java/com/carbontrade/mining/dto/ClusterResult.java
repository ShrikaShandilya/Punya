package com.carbontrade.mining.dto;

public class ClusterResult {

    private int k;
    private int[] labels;
    private double[][] centroids;

    public ClusterResult() {}

    public ClusterResult(int k, int[] labels, double[][] centroids) {
        this.k = k;
        this.labels = labels;
        this.centroids = centroids;
    }

    public int getK() { return k; }
    public void setK(int k) { this.k = k; }

    public int[] getLabels() { return labels; }
    public void setLabels(int[] labels) { this.labels = labels; }

    public double[][] getCentroids() { return centroids; }
    public void setCentroids(double[][] centroids) { this.centroids = centroids; }
}
