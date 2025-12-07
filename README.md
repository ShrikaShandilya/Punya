## Punya

This repo holds an API (backend -> Java) and an extension, along with a frontend application (Python).

### Frontend Application (`test.py`)

The frontend is a robust desktop dashboard built with **Python** and **PyQt6** that acts as a "Smart Meter" for your computer. It interfaces with the Spring Boot backend to track carbon footprints, monitor system resources, and manage user accounts.

**Key Capabilities & Architecture:**
*   **System Monitoring & Carbon Tracking:** Uses `psutil` in a background `SystemMonitor` thread to poll real-time CPU, Memory, and Disk usage. These metrics are converted into estimated CO2 emissions and "efficiency points" using custom algorithms.
*   **Interactive Dashboard:** A responsive, dark-mode UI that visualizes system health, efficiency rewards, and browser resource usage (profiling Chrome, Firefox, etc. for memory leaks via `BrowserMonitor`).
*   **User & Data Management:** Allows users to register, view Carbon Credit (CER) balances, and check market prices. Telemetry data is aggregated locally and can be uploaded as CSVs for detailed backend analysis using the non-blocking `ApiWorker`.
*   **Tech Stack:** Python 3.x, PyQt6 (GUI), `psutil` (Monitoring), `requests` (REST API Client).
