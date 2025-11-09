# CarbonTrade API

Carbon credit trading platform with ML-powered behavioral analytics for tracking, analyzing, and detecting anomalies in user carbon footprint patterns.

## Overview

CarbonTrade provides a comprehensive REST API for managing carbon credit trading (CER tokens) and analyzing carbon emission data using machine learning. The platform combines real-time market pricing with advanced data mining capabilities to identify behavioral patterns and unusual carbon usage.

## How Mining Works

The mining subsystem derives insights directly from daily carbon emission records stored in the database. Its behavior is fully grounded in the source code you provided.

### 1. CSV Ingestion (IngestionService)

* Expects CSV columns: `userId,date,category,amount,unit,emissionFactor(optional)`.
* If `emissionFactor` is missing, a category‑based default is used (e.g., electricity 0.417 kgCO₂e/kWh).
* Each record is converted into a `CarbonFootprint` entity, where `kgCO2e = amount * emissionFactor`.
* All rows are saved in bulk via `CarbonFootprintRepository`.

### 2. Feature Engineering (FeatureEngineeringService)

For each day in the user-selected date range, the system constructs a **feature vector**:

1. **Category one-hot totals** – sum of kgCO₂e per category for that day. Categories are learned dynamically from the data.
2. **Month-wide total** – total kgCO₂e for the entire month of that day.
3. **Weekday** – numeric day of week (1–7).
4. **Month** – month number (1–12).
5. **Total daily kgCO₂e** – the sum of all categories.

Vector layout:

```
[monthTotal, weekday, month, todaySum, ...categoryTotals]
```

One vector is produced *for every day*, even if emissions are zero.

### 3. Clustering (MiningService)

* A Tribuo **KMeansTrainer** is built using:

  * L2 (Euclidean) distance
  * random initialisation
  * 100 iterations
  * 10 restarts
* All feature vectors are added to a Tribuo `MutableDataset`.
* The trainer produces:

  * **labels** → cluster assignment for each day
  * **centroids** → representative centroid vector for each cluster
* These outputs are returned to the API as a `ClusterResult`.

### 4. Anomaly Scoring (MiningService)

An anomaly score is computed as:

```
min(distance(point, centroid[i])) for all centroids
```

This is simply the shortest Euclidean distance between the day's feature vector and any cluster centroid.

High-distance points are more unusual.

### 5. Anomaly Filtering

* Scores are sorted.
* The **85th percentile threshold** is computed.
* Only points with scores **>= threshold** are classified as anomalies.
* Results are sorted descending by severity.
* Returned as a list of `AnomalyPoint`.

### 6. Insights Summary

The `Insights` DTO generates an automatic summary:

```
"Analyzed X records across Y behavioral patterns and found Z unusual activity periods."
```

---

## Tech Stack

**Backend**

* **Java 21** with **Spring Boot 3.2** for modern JVM features, native-image readiness, and streamlined application configuration.
* **Spring Security** for authentication and authorization.

**Machine Learning / Analytics**

* **Tribuo** for clustering and anomaly detection, primarily using **K-means**.
* Chosen for its JVM-native design, transparent model provenance, and simple integration with the analytics pipeline.

**Database**

* **H2 in-memory database** with **JPA/Hibernate** for rapid development, quick schema iteration, and fast test cycles without external dependencies.

**Build & Dependency Management**

* **Maven**, selected for its predictable build lifecycle, broad plugin ecosystem, and straightforward CI/CD integration.
* Its explicit configuration model simplifies reproducible builds and dependency governance.

## Quick Start

```bash
# Build and run
mvn clean package -DskipTests
mvn spring-boot:run

# Run tests
./scripts/testing.sh
```

Server runs on `http://localhost:8080`

## API Endpoints

### Market API

Manage carbon credit pricing and market data.

**`GET /api/market/price`**

* Returns current CER (Certified Emission Reduction) token price
* Dynamic pricing between $20-$30 per tonne of CO₂
* Response includes price and currency

```json
{"pricePerCER": 25.68, "currency": "USD"}
```

### User Management

Handle user registration, authentication, and profile management.

**`POST /api/users/register`**

* Register new user account
* Body: `{"name": "string", "email": "string", "password": "string"}`
* Returns user ID and success confirmation

**`POST /api/users/login`**

* Authenticate user credentials
* Body: `{"email": "string", "password": "string"}`
* Returns user profile with CER balance on success

**`GET /api/users/{userId}/profile`**

* Retrieve user profile information
* Includes name, email, and current CER token balance
* Returns 404 if user not found

### Data Mining & Analytics

ML-powered carbon footprint analysis with clustering and anomaly detection.

CarbonTrade's mining subsystem processes user-submitted carbon emission records to extract behavioral patterns. Each record typically includes activity-specific emission values (for example, transportation, energy consumption, and industrial actions). The system normalizes these values, constructs feature vectors, and feeds them into Tribuo’s K-means clustering algorithm. Groupings reveal typical daily or periodic emission behaviors.

Anomaly detection leverages distance-based scoring: records that deviate significantly from assigned cluster centroids are flagged as unusual usage periods, helping identify abnormal carbon spikes, potential reporting inconsistencies, or unexpected behavioral shifts.

**`GET /mining/health`**

* Health check endpoint for mining subsystem
* Returns operational status

**`POST /mining/ingest`**

* Upload carbon footprint data via CSV file
* Accepts multipart/form-data with `file` parameter
* Processes and stores carbon emission records
* Returns count of successfully processed records
* Validates file presence and format

```json
{"success": true, "recordsProcessed": 366, "message": "Successfully processed 366 records"}
```

**`GET /mining/analyze`**

* Analyze carbon usage patterns using K-means clustering and anomaly detection
* **Query Parameters**:

  * `userId` (required): User ID to analyze
  * `start` (required): Start date (YYYY-MM-DD)
  * `end` (required): End date (YYYY-MM-DD)
  * `k` (optional, default=4): Number of clusters for K-means
* **Returns**:

  * **Clusters**: Behavioral groupings with labels and centroids
  * **Anomalies**: Unusual carbon usage periods with anomaly scores
  * **Summary**: Total records analyzed, cluster count, anomaly count

```json
{
  "clusters": {
    "k": 4,
    "labels": [0, 1, 2, ...],
    "centroids": [[0.0, 2.01, 3.61, 0.0], ...]
  },
  "anomalies": [
    {"date": "2024-08-05", "score": 3.70},
    ...
  ],
  "summary": "Analyzed 366 records across 4 behavioral patterns and found 57 unusual activity periods.",
  "totalRecords": 366,
  "totalClusters": 4,
  "totalAnomalies": 57
}
```

## Testing

Run comprehensive API tests:

```bash
./scripts/testing.sh
```

Tests include positive/negative cases with clean pass/fail output.
