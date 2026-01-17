# Punya / CarbonTrade System

This repository contains the **CarbonTrade** platform, a sustainable trading and analytics ecosystem.

## Components

### 1. CarbonTrade API (`/carbontrade`)
The core backend service powered by **Java 21**, **Spring Boot 3**, and **SQLite**.
- **Features**: User authentication, Carbon Credit (CER) trading, ML-based anomaly detection (Tribuo).
- **Port**: `8081`
- **Persistence**: `carbontrade.db` (SQLite)

### 2. CarbonTrade GUI (`test.py`)
A modern **PyQt6** desktop client for interacting with the API.
- **Features**: Real-time System Monitoring, Browser Efficiency Tracking, and Market Dashboard.
- **Privacy**: Includes explicit user consent dialogs for all monitoring features.

---

## Getting Started

### Prerequisites
- Java 21+
- Maven
- Python 3.10+
- PyQt6 (`pip install PyQt6 requests psutil`)

### Running the System

**1. Start the Backend:**
```bash
cd carbontrade
mvn spring-boot:run
```

**2. Start the Client:**
Open a new terminal:
```bash
# Install dependencies if needed
pip install PyQt6 requests psutil pyqtgraph

# Run the GUI
python3 test.py
```

## Security & Privacy
- **Passwords**: Hashed using BCrypt.
- **Data**: Stored locally in `carbontrade.db`.
- **Monitoring**: Process and Browser scanning require explicit one-time consent per session.
