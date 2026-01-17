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

### 1. AuthController (`AuthController.java`) 

Manages public authentication and account recovery.

**Base URL:** `/api/v1/auth`

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/register` | Registers a new user. | `{"username": "...", "email": "...", "password": "..."}` | Success message string. |
| `POST` | `/login` | Authenticates a user and returns a JWT. | `{"username": "...", "password": "..."}` | `{"accessToken": "...", "tokenType": "Bearer"}` |
| `POST` | `/refresh` | Refreshes an expired JWT. | `refreshToken` string | New access token string. |
| `GET` | `/verify` | Verifies if a token is valid. | `token` (Query Param) | Validation message string. |
| `POST` | `/forgot-password` | Initiates password reset flow. | `email` string | Success message. |

### 2. UserController (`UserController.java`)

Manages user profiles and administration. Most endpoints now require ADMIN role.

**Base URL:** `/api/users`

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/{id}` | Retrieves a user's details. | N/A | `User` object or 404. |
| `POST` | `/` | Creates a new user (Admin). | `User` object | Created `User` object. |
| `PUT` | `/{id}` | Updates an existing user. | `User` object | Updated `User` object. |
| `DELETE` | `/{id}` | Deletes a user. | N/A | 204 No Content. |

### 3. KycController (`KycController.java`) (NEW)

Manages Know Your Customer (KYC) document uploads and verification status.

**Base URL:** `/api/v1/users/kyc`

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/upload` | Uploads a KYC document (Passport/ID). | `file` (MultipartFile) | Success message with filename. |
| `GET` | `/status` | Checks the current KYC status of the logged-in user. | N/A | Status string (e.g., "PENDING", "SUBMITTED"). |

### 4. CarbonController (`CarbonController.java`)

Handles carbon footprint calculations.

**Base URL:** `/api/carbon`

> **Design Note**: This controller is stateless by design. It performs pure calculations without side effects.

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/calculate` | Calculates carbon footprint. | `{"electricity": 100, "travel": 50, "dietType": 1}` | Breakdown of emissions and total CO2e. |
| `GET` | `/info` | Information about calculation factors. | N/A | Map of conversion factors. |

### 5. MarketController (`MarketController.java`)

Facilitates the trading of Carbon Emission Reduction (CER) tokens.

**Base URL:** `/api/market`

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/price` | Gets the current market price of CER tokens. | N/A | `{"pricePerCER": ..., "currency": "USD"}` |
| `POST` | `/buy` | Allows users to buy CER tokens. | `{"userId": ..., "amount": ...}` | Transaction details. |
| `POST` | `/sell` | Allows users to sell CER tokens. | `{"userId": ..., "amount": ...}` | Transaction details. |

### 6. MiningController (`MiningController.java`)

Handles data ingestion and ML-based analysis.

**Base URL:** `/mining`

| Method | Endpoint | Description | Request Parameters | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/health` | Health check for mining subsystem. | N/A | Status message. |
| `POST` | `/ingest` | Uploads CSV data for analysis. | `file` (MultipartFile) | Record count. |
| `GET` | `/analyze` | Performs K-Means clustering. | `userId`, `start`, `end`, `k` | `Insights` object. |

### 7. PointsController (`PointsController.java`)

Manages the reward points system.

**Base URL:** `/api/points`

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/rules` | Returns point conversion rules. | N/A | `RulesResponse` |
| `GET` | `/balance` | Retrieves points balance. | `userId` (Query Param) | `BalanceResponse` |
| `POST` | `/earn` | Awards points. | `{"userId": ..., "amount": ..., "reason": "..."}` | Updated `BalanceResponse`. |

### 8. TransactionController (`TransactionController.java`)

Provides access to transaction history.

**Base URL:** `/api/transactions`

| Method | Endpoint | Description | Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/user/{userId}` | Lists all transactions for a specific user. | List of `Transaction` objects. |
| `GET` | `/all` | Lists all transactions in the system. | List of `Transaction` objects. |

---

## Key Java Classes & Responsibilities

### Models (`com.carbontrade.model`)
-   **`User.java`**: Represents a registered user. **Updated** to include `role` (USER/ADMIN) and `kycStatus`.
-   **`CarbonFootprint.java`**: Represents a single carbon footprint record.
-   **`Transaction.java`**: Records market buy/sell events.
-   **`PointsWallet.java`**:  Stores current points and coin balances.

### Services (`com.carbontrade.service`)
-   **`AuthService.java`**: **(New)** Centralized logic for login, registration, password management, and email verification.
-   **`KycService.java`**: **(New)** Handles document storage and KYC status updates.
-   **`UserService.java`**: Business logic for users.
-   **`MarketService.java`**: Logic for CER pricing.
-   **`PointsService.java`**: Logic for earning points.

### Security (`com.carbontrade.security`)
-   **`JwtTokenProvider.java`**: Handles generation and validation of JWTs.
-   **`JwtTokenFilter.java`**: Intercepts requests to validate tokens.
-   **`SecurityConfig.java`**: Configures Spring Security (RBAC, public endpoints).
