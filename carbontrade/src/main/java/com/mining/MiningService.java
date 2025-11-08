package com.carbontrade.mining;

import com.carbontrade.mining.dto.AnomalyPoint;
import com.carbontrade.mining.dto.ClusterResult;
import com.carbontrade.mining.dto.Insights;
import org.springframework.stereotype.Service;
import org.tribuo.Dataset;
import org.tribuo.Example;
import org.tribuo.Feature;
import org.tribuo.MutableDataset;
import org.tribuo.clustering.ClusterID;
import org.tribuo.clustering.kmeans.KMeansTrainer;
import org.tribuo.math.distance.DistanceType;
import org.tribuo.math.distance.L2Distance;
import org.tribuo.provenance.SimpleDataSourceProvenance;
import org.tribuo.clustering.ClusteringFactory;
import org.tribuo.impl.ArrayExample;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class MiningService {

    private final FeatureEngineeringService featureEngineering;

    public MiningService(FeatureEngineeringService featureEngineering) {
        this.featureEngineering = featureEngineering;
    }

    public Insights analyze(Long userId, LocalDate start, LocalDate end, int k) {

        var rows = featureEngineering.buildDailyFeatures(userId, start, end);
        if (rows.isEmpty()) {
            return new Insights(null, List.of(), 0);
        }

        // Convert data to Tribuo format
        ClusteringFactory factory = new ClusteringFactory();
        MutableDataset<ClusterID> dataset = new MutableDataset<>(
            new SimpleDataSourceProvenance("Carbon Trade Data", OffsetDateTime.now(), factory),
            factory
        );

        for (FeatureEngineeringService.FeatureRow row : rows) {
            double[] featureValues = row.features();
            Example<ClusterID> example = new ArrayExample<>(new ClusterID(0));
            for (int i = 0; i < featureValues.length; i++) {
                example.add(new Feature("feature-" + i, featureValues[i]));
            }
            dataset.add(example);
        }

        // --- KMEANS ---
        KMeansTrainer trainer = new KMeansTrainer(k, 100, new L2Distance(), KMeansTrainer.Initialisation.RANDOM, 10, System.currentTimeMillis());
        var model = trainer.train(dataset);
        var predictions = model.predict(dataset);
        
        int[] clusters = predictions.stream()
            .mapToInt(p -> p.getOutput().getID())
            .toArray();
            
        // Get centroids from the trained model
        double[][] centroids = new double[k][];
        for (int i = 0; i < k; i++) {
            var centroidExample = model.getCentroids().get(i);
            double[] centroidFeatures = new double[centroidExample.size()];
            int j = 0;
            for (Feature f : centroidExample) {
                centroidFeatures[j++] = f.getValue();
            }
            centroids[i] = centroidFeatures;
        }

        ClusterResult clusterResult = new ClusterResult(k, clusters, centroids);

        // --- ANOMALY DETECTION ---
        // Using statistical approach for anomaly detection
        double[] scores = IntStream.range(0, rows.size())
            .mapToDouble(i -> {
                double[] point = rows.get(i).features();
                return calculateAnomalyScore(point, centroids);
            })
            .toArray();

        List<AnomalyPoint> anomalies = IntStream.range(0, scores.length)
            .mapToObj(i -> new AnomalyPoint(rows.get(i).date(), scores[i]))
            .toList();

        // Top ~15% anomalies
        double threshold = percentile(scores, 85);

        List<AnomalyPoint> topAnomalies = anomalies.stream()
                .filter(a -> a.score() >= threshold)
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .toList();

        return new Insights(clusterResult, topAnomalies, rows.size());

    }

    private double calculateAnomalyScore(double[] point, double[][] centroids) {
        double minDistance = Double.MAX_VALUE;
        for (double[] centroid : centroids) {
            double distance = 0;
            for (int i = 0; i < point.length; i++) {
                distance += Math.pow(point[i] - centroid[i], 2);
            }
            distance = Math.sqrt(distance);
            minDistance = Math.min(minDistance, distance);
        }
        return minDistance;
    }

    private double percentile(double[] data, double p) {
        double[] copy = data.clone();
        Arrays.sort(copy);
        int index = (int) Math.ceil((p / 100.0) * (copy.length - 1));
        index = Math.max(0, Math.min(index, copy.length - 1));
        return copy[index];
    }
}
