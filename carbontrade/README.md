# CarbonTrade API

CarbonTrade is a carbon-credit trading and analytics platform that combines market data, user management, and machine-learning–powered behavioral analysis. It enables users to track carbon emissions, detect anomalies in footprint behavior, earn reward points, and automatically mint CarbonTrade Coins.

The system is optimized for clarity, extensibility, and analytical rigor, while maintaining a clean, well-structured REST API.

---

## Features

### Carbon Credit Market
- Dynamic CER token pricing
- Real-time pricing API

### User Management
- Registration
- Login
- Profile management
- CER token balances

### ML-Powered Analytics
- CSV ingestion for carbon footprint data
- K-Means clustering for behavioral segmentation
- Anomaly detection based on cluster-distance scoring
- Analytical summaries and insights

### Points & Coins Reward System
- Points automatically awarded after ingestion and analysis
- 50 points automatically convert into 1 CarbonTrade Coin
- Full transaction logging (earn + auto-convert)
- User-friendly balance endpoints

### Automated Test Suite
- Full API test runner
- Validates all endpoints
- Provides formatted PASS/FAIL summary
- JQ-compatible JSON output

---

## Tech Stack

### Java 21  
Modern, high-performance runtime with:
- Improved GC performance  
- Virtual-thread optimizations  
- Strong security baseline  

### Spring Boot 3.2  
Provides:
- REST API framework  
- Auto-configuration  
- Actuator endpoints  
- Spring Security integration  
- Built-in observability  

### Tribuo (Oracle Labs ML Engine)  
Used for:
- Data ingestion and feature extraction  
- K-Means clustering  
- Centroid-distance anomaly detection  
- Vectorized analysis operations  

Tribuo was selected for:
- Strong typing for datasets & models
- Clear reproducibility
- JVM-native performance

### H2 In-Memory Database  
Used during development/testing for:
- User table  
- Points wallet  
- Points transaction logs  
- Ingested carbon footprint records  

### JPA/Hibernate  
- ORM mapping  
- Automatic schema creation  
- Repository abstractions  

### Spring Security  
Configuration includes:
- Public endpoints for:
  - `/api/market/**`
  - `/api/users/**`
  - `/mining/**`
  - `/api/points/**`
  - `/actuator/**`
- CSRF disabled for API clients
- Debug enabled for developmental transparency

### Maven  
Used for:
- Dependency management  
- Packaging  
- Build lifecycle  
- Plugin integrations  

### Testing Stack  
- Bash scripting  
- `curl` for HTTP operations  
- `jq` for JSON parsing  
- Exit-code based PASS/FAIL control  

---

## Quick Start

### Build & Run
```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

Server runs at:
```
http://localhost:8081
```

### Run All Tests
```bash
./scripts/testing.sh
```

---

## API Documentation

# MARKET API

### GET /api/market/price
Returns current CER trading price.

**Response**
```json
{
  "pricePerCER": 25.68,
  "currency": "USD"
}
```

---

# USER API

### POST /api/users/register
Registers a new user.

```json
{
  "name": "alice",
  "email": "alice@test.com",
  "password": "test123"
}
```

### POST /api/users/login  
Authenticates user credentials.

### GET /api/users/{id}/profile  
Returns full user profile including CER balance.

---

# MINING API

### GET /mining/health  
Operational status check.

### POST /mining/ingest  
Upload CSV data.

```bash
curl -F "file=@data.csv" http://localhost:8081/mining/ingest
```

Example response:
```json
{
  "success": true,
  "recordsProcessed": 366,
  "message": "Successfully processed 366 records"
}
```

### GET /mining/analyze  
Perform clustering + anomaly detection.

Response:
```json
{
  "clusters": { "k": 4, "labels": [...], "centroids": [...] },
  "anomalies": [ {"date":"2024-08-05","score":3.70} ],
  "summary":"Analyzed 366 records across 4 patterns and found 57 anomalies.",
  "totalRecords":366,
  "totalClusters":4,
  "totalAnomalies":57
}
```

---

# POINTS & COINS SYSTEM

### Reward Model
- Every API ingestion → points awarded  
- Every analysis → points awarded  
- Every **50 points auto-converts into 1 coin**  

### GET /api/points/rules
Returns static reward rules.

### GET /api/points/balance?userId=1  
Returns:

```json
{
  "userId": 1,
  "points": 12,
  "coins": 3
}
```

### POST /api/points/earn  
Dev-only manual award endpoint.

```json
{
  "userId": 1,
  "amount": 50,
  "reason": "ingestion_bonus"
}
```

---

## Testing Script

Located at:
```
/scripts/testing.sh
```

Executes:
- Market tests  
- User tests  
- Mining tests  
- Points tests  
- Negative-case tests  
- Summary with PASS/FAIL  

---

## Project Structure
```
src/main/java/com/carbontrade/
├── controller/
├── mining/
├── service/
├── model/
├── repository/
└── config/
```

