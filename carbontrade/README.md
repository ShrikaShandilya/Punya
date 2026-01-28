# CarbonTrade API

CarbonTrade is a carbon-credit trading and analytics platform that combines market data, user management, and machine-learning–powered behavioral analysis. It enables users to track carbon emissions, detect anomalies in footprint behavior, earn reward points, and automatically mint CarbonTrade Coins.

The system is optimized for clarity, extensibility, and analytical rigor, while maintaining a clean, well-structured REST API.

---

## Key Features

### 🔒 Security & Privacy
- **Secure Authentication**: BCrypt password hashing and role-based access control (RBAC).
- **Privacy-First Design**: Explicit user consent flow for system and browser process monitoring.
- **Protected Data**: Sensitive endpoints (like listing all users) are restricted to ADMIN content.

### 💾 Persistence (New!)
- **SQLite Database**: Zero-config, file-based persistence using `carbontrade.db`.
- **Data Safety**: User accounts and balances survive application restarts.

### 📈 Carbon Credit Market
- Dynamic CER token pricing
- Real-time pricing API

### 🧠 ML-Powered Analytics
- CSV ingestion for carbon footprint data
- K-Means clustering for behavioral segmentation
- Anomaly detection based on cluster-distance scoring
- Analytical summaries and insights

---

## Tech Stack

### Java 21 & Spring Boot 3.2
- **Runtime**: Java 21 (Virtual Threads)
- **Framework**: Spring Boot 3.2
- **Database**: SQLite (Production/Dev)
- **Security**: Spring Security + JJWT
- **ML Engine**: Oracle Tribuo

---

## Quick Start

### 1. Build & Run API
The server runs on **Port 8081** to avoid conflicts.

```bash
cd carbontrade
mvn clean package -DskipTests
mvn spring-boot:run
```

Server: `http://localhost:8081`

### 2. Run GUI Client
We provide a Python-based GUI for easy interaction (requires PyQt6).

```bash
cd ..
python3 test.py
```
*Note: You will be asked for consent before any system monitoring starts.*

---

## API Documentation

### Full Documentation
For a detailed breakdown of all endpoints, parameters, and models, please refer to the [API Documentation](API_DOCUMENTATION.md).

### API Overview

- **User API**: Registration, login, and profile management.
- **Carbon API**: Calculate footprints for electricity, travel, and diet.
- **Market API**: Real-time pricing and trading (Buy/Sell) of CER tokens.
- **Mining API**: Upload CSV data for ML analysis (Clustering & Anomaly Detection).
- **Points API**: Earn EcoPoints and view coin balances.
- **Transaction API**: View history of market trades.

#### Quick Examples:

**Register User:**
`POST /api/users/register`
```json
{
  "name": "alice",
  "email": "alice@test.com",
  "password": "test123"
}
```

**Ingest Data:**
`POST /mining/ingest`
```bash
curl -F "file=@data.csv" http://localhost:8081/mining/ingest
```

---

## Project Structure
```
src/main/java/com/carbontrade/
├── controller/       # REST Endpoints
├── mining/          # ML & Analysis Services
├── service/         # Business Logic
├── model/           # JPA Entities (User, Transaction)
├── repository/      # Data Access Layer
└── config/          # Security & App Config
```

## Author

- **Eshita Srivastava (eshi999)**: API Development & Design
