# CarbonTrade API

Carbon credit trading platform with ML-powered behavioral analytics for tracking, analyzing, and detecting anomalies in user carbon footprint patterns.

## Overview

CarbonTrade provides a comprehensive REST API for managing carbon credit trading (CER tokens) and analyzing carbon emission data using machine learning. The platform combines real-time market pricing with advanced data mining capabilities to identify behavioral patterns and unusual carbon usage.

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
