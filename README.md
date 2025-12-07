## Punya

This repo holds an API (backend -> Java) and an extension, along with a frontend application (Python).

### Frontend Application (`test.py`)

The frontend is a high-performance desktop client engineered with **Python 3** and **PyQt6**, implementing a multi-threaded architecture to ensure non-blocking UI responsiveness while performing intensive real-time telemetry.

**Technical Architecture & Components:**

*   **Asynchronous Concurrency Model:**
    *   **Core Event Loop:** Powered by PyQt6, managing the application lifecycle and UI rendering.
    *   **Worker Threads (`QThread`):** Heavy I/O and CPU-bound tasks are offloaded to dedicated background threads to prevent UI freezing.
        *   `SystemMonitor`: A daemon thread utilizing `psutil` to sample low-level OS metrics (CPU interrupts, memory page faults, disk I/O) at 1Hz, computing instantaneous Carbon Intensity algorithms.
        *   `ApiWorker`: Handles blocking network I/O. serialization, and RESTful HTTP transactions with the Spring Boot backend (`/api/users`, `/mining/ingest`).
        *   `BrowserMonitor`: Performs heuristic analysis of the process table to identify browser engine signatures (Chromium/Gecko variants) and profile their resource efficiency.

*   **Data Pipeline & Ingestion:**
    *   Aggregates local system telemetry into time-series datasets.
    *   Implements buffered batch uploading of CSV payloads to the backend for ML-driven anomaly detection.
    *   Uses Qt Signals/Slots (`pyqtSignal`) for thread-safe cross-context communication between workers and the main GUI thread.

*   **Tech Stack:** Python 3.12+, PyQt6 (Widgets, Core), `psutil` (System Interface), `requests` (HTTP Client), `pandas` (Data Buffering).
