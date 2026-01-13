# CarbonTrade API Documentation

This document provides a detailed description of the CarbonTrade API, including the purpose of key Java files and the functionality of each endpoint.

## Project Structure Overview

The project is organized into standard Spring Boot layers:

-   **`com.carbontrade.controller`**: REST controllers that handle HTTP requests and define API endpoints.
-   **`com.carbontrade.service`**: Service layer containing business logic.
-   **`com.carbontrade.model`**: Data models (JPA entities) representing the domain objects.
-   **`com.carbontrade.mining`**: specialized package for machine learning and data analysis tasks.
-   **`com.carbontrade.repository`**: Data access interfaces.

---

## Controllers & API Endpoints

### 1. UserController (`UserController.java`)

Manages user registration, authentication, and profile retrieval.

**Base URL:** `/api/users`


> **Design Note**: We separate User management to handle authentication and profile data independently from the carbon logic. This ensures that security concerns (passwords, roles) are isolated from business logic.

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/register` | Registers a new user. If the user exists, it attempts to log them in. | `{"name": "...", "email": "...", "password": "..."}` | `{"userId": ..., "message": "..."}` |
| `POST` | `/login` | Authenticates a user. | `{"email": "...", "password": "..."}` | User details and success status or error message. |
| `GET` | `/{userId}/profile` | Retrieves a user's profile details. | N/A | `User` object or 404 Not Found. |
| `GET` | `/` | Retrieves a list of all users. | N/A | List of `User` objects. |

### 2. CarbonController (`CarbonController.java`)

Handles carbon footprint calculations.

**Base URL:** `/api/carbon`


> **Design Note**: This controller is stateless by design. It performs pure calculations without side effects (database saves), allowing the frontend to show "what-if" scenarios to users before they commit their data.

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/calculate` | Calculates carbon footprint based on electricity, travel, and diet. | `{"electricity": 100, "travel": 50, "dietType": 1}` | Breakdown of emissions and total CO2e in tonnes. |
| `GET` | `/info` | Provides information about calculation factors. | N/A | Map of conversion factors and diet types. |

### 3. MarketController (`MarketController.java`)

Facilitates the trading of Carbon Emission Reduction (CER) tokens.

**Base URL:** `/api/market`


> **Design Note**: Market operations are distinct from simple database CRUD. They require transactional consistency (updating user balance AND creating a transaction record simultaneously), which is why we dedicated a specific controller to handle these atomic trade operations.

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/price` | Gets the current market price of CER tokens. | N/A | `{"pricePerCER": ..., "currency": "USD"}` |
| `POST` | `/buy` | Allows users to buy CER tokens. | `{"userId": ..., "amount": ...}` | Transaction details and success message. |
| `POST` | `/sell` | Allows users to sell CER tokens. | `{"userId": ..., "amount": ...}` | Transaction details and success message. |

### 4. MiningController (`MiningController.java`)

Handles data ingestion and ML-based analysis of carbon footprint data.

**Base URL:** `/mining`


> **Design Note**: We separated Mining (Analysis) from the standard API because these operations are computationally heavy (ML training/inference). Isolating them prevents analytical queries from slowing down the responsive user-facing endpoints.

| Method | Endpoint | Description | Request Parameters | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/health` | specific health check for the mining subsystem. | N/A | "Mining subsystem operational" |
| `POST` | `/ingest` | Uploads a CSV file containing carbon data for analysis. | `file` (MultipartFile) | Number of records processed. |
| `GET` | `/analyze` | Performs K-Means clustering and anomaly detection on user data. | `userId`, `start` (date), `end` (date), `k` (clusters) | `Insights` object containing clusters and anomalies. |

### 5. PointsController (`PointsController.java`)

Manages the reward points system (EcoPoints and Coins).

**Base URL:** `/api/points`


> **Design Note**: Points and Gamification are kept separate to make the reward system pluggable. We can change the rules of earning points without modifying the core `Carbon` or `User` logic.

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/rules` | Returns the rules for point conversion (e.g., points per coin). | N/A | `RulesResponse` |
| `GET` | `/balance` | Retrieves the points and coins balance for a user. | `userId` (Query Param) | `BalanceResponse` |
| `POST` | `/earn` | Awards points to a user for specific actions. | `{"userId": ..., "amount": ..., "reason": "..."}` | Updated `BalanceResponse`. |

### 6. TransactionController (`TransactionController.java`)

Provides access to transaction history.

**Base URL:** `/api/transactions`

| Method | Endpoint | Description | Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/user/{userId}` | Lists all transactions for a specific user. | List of `Transaction` objects. |
| `GET` | `/{transactionId}` | Retrieves details of a specific transaction. | `Transaction` object. |
| `GET` | `/all` | Lists all transactions in the system. | List of `Transaction` objects. |

---

## Key Java Classes & Responsibilities

### Models (`com.carbontrade.model`)
-   **`User.java`**: Represents a registered user, including credentials and CER balance.
-   **`CarbonFootprint.java`**: Represents a single carbon footprint record (Electricity, Travel, etc.) with CO2e calculations.
-   **`Transaction.java`**: Records market buy/sell events.
-   **`PointsWallet.java`**:  Stores current points and coin balances for a user.
-   **`PointsTransaction.java`**: History of points earned or spent.

### Services (`com.carbontrade.service`)
-   **`UserService.java`**: Business logic for users (login, register, balance updates).
-   **`MarketService.java`**: logic for determining CER prices (can be dynamic or static).
-   **`PointsService.java`**: Logic for earning points and converting them to coins.
-   **`TransactionService.java`**: Helper to create and retrieve transaction records.

### Mining / Analysis (`com.carbontrade.mining`)
-   **`IngestionService.java`**: Parses CSV files and saves `CarbonFootprint` data.
-   **`MiningService.java`**: Connects to Oracle Tribuo or other ML libraries to Run K-Means clustering and detect anomalies.
-   **`FeatureEngineeringService.java`**: prepares raw data for the ML model.
