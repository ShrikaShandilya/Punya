import sys
import psutil
import platform
import requests 
import csv
import os
import json
from datetime import datetime
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QTextEdit, QTabWidget,
    QProgressBar, QGroupBox, QListWidget, QListWidgetItem, QTableWidget, QTableWidgetItem,
    QMessageBox, QDialog, QHeaderView, QLineEdit, QFormLayout, QComboBox,
    QScrollArea, QSizePolicy
)
from PyQt6.QtCore import Qt, QThread, pyqtSignal, QTimer, QEvent
from PyQt6.QtGui import QFont, QColor

# ------------------- Configuration -------------------
API_BASE_URL = "http://localhost:8081"
CURRENT_SELECTED_USER_ID = 1  

# ------------------- Dark Mode Style Sheet -------------------
DARK_MODE_STYLESHEET = """
QMainWindow, QWidget, QTabWidget::pane {
    background-color: #2e3436;
    color: #eeeeec;
}
QScrollArea {
    border: none;
    background-color: #2e3436;
}
QScrollArea > QWidget > QWidget {
    background-color: #2e3436;
}
QTabWidget::tab-bar {
    alignment: left;
}
QTabBar::tab {
    background: #343a40;
    color: #ffffff;
    padding: 8px 15px;
    margin-right: 1px;
    border-top-left-radius: 4px;
    border-top-right-radius: 4px;
}
QTabBar::tab:selected {
    background: #4e9a06;
    font-weight: bold;
}
QTabBar::tab:hover {
    background: #40454a;
}
QGroupBox {
    border: 2px solid #555753;
    border-radius: 5px;
    margin-top: 20px; 
    font-weight: bold;
    color: #babdb6;
}
QGroupBox::title {
    subcontrol-origin: margin;
    subcontrol-position: top center;
    padding: 0 5px;
    background-color: #2e3436;
}
QProgressBar {
    border: 1px solid #555753;
    border-radius: 5px;
    text-align: center;
    color: #ffffff;
    background-color: #555753;
    min-height: 20px;
}
QProgressBar::chunk {
    background-color: #ff8c00;
    border-radius: 5px;
}
QPushButton {
    background-color: #4e9a06;
    color: white;
    font-weight: bold;
    min-height: 30px; 
    border-radius: 5px;
    padding: 6px 12px; 
}
QPushButton:hover {
    background-color: #73d216;
}
QPushButton:disabled {
    background-color: #555753;
    color: #a9a9a9;
}
QLineEdit, QComboBox {
    background-color: #1e1e1e;
    color: #ffffff;
    border: 1px solid #555753;
    border-radius: 4px;
    padding: 6px;
    min-height: 25px; 
}
QComboBox::drop-down {
    border: 0px;
}
QTextEdit, QListWidget, QTableWidget {
    background-color: #1e1e1e;
    color: #ffffff;
    border: 1px solid #555753;
    border-radius: 4px;
    padding: 5px;
    gridline-color: #555753;
}
QHeaderView::section {
    background-color: #343a40;
    color: #eeeeec;
    padding: 4px;
    border: 1px solid #555753;
    font-weight: bold;
}
QTableWidget::item {
    padding: 5px;
}
QTableWidget::item:selected {
    background-color: #4e9a06;
}
"""

# ------------------- API Worker Thread -------------------
class ApiWorker(QThread):
    result_signal = pyqtSignal(bool, str, dict) 

    def __init__(self, action, payload=None, file_path=None):
        super().__init__()
        self.action = action
        self.payload = payload
        self.file_path = file_path

    def run(self):
        try:
            if self.action == "get_market_price":
                url = f"{API_BASE_URL}/api/market/price"
                response = requests.get(url, timeout=3)
                if response.status_code == 200:
                    self.result_signal.emit(True, "Price Updated", response.json())
                else:
                    self.result_signal.emit(False, "API Error", {})

            elif self.action == "earn_points":
                url = f"{API_BASE_URL}/api/points/earn"
                headers = {'Content-Type': 'application/json'}
                response = requests.post(url, json=self.payload, headers=headers, timeout=3)
                if response.status_code == 200:
                    self.result_signal.emit(True, "Points Synced", {})
                else:
                    self.result_signal.emit(False, f"Sync Failed: {response.status_code}", {})

            elif self.action == "register_user":
                url = f"{API_BASE_URL}/api/users/register"
                headers = {'Content-Type': 'application/json'}
                response = requests.post(url, json=self.payload, headers=headers, timeout=5)
                if response.status_code == 200:
                    self.result_signal.emit(True, "Registration Successful", response.json())
                else:
                    self.result_signal.emit(False, f"Reg Failed: {response.status_code} - {response.text}", {})

            elif self.action == "upload_csv":
                url = f"{API_BASE_URL}/mining/ingest"
                if os.path.exists(self.file_path):
                    with open(self.file_path, 'rb') as f:
                        files = {'file': (os.path.basename(self.file_path), f, 'text/csv')}
                        data = {'userId': str(self.payload.get('userId', 1))}
                        response = requests.post(url, files=files, data=data, timeout=10)
                        
                        if response.status_code == 200:
                            self.result_signal.emit(True, "CSV Uploaded", response.json())
                        else:
                            try:
                                err_text = response.json().get('error', response.text[:100])
                            except:
                                err_text = response.text[:100]
                            error_msg = f"Upload Failed: {response.status_code} - {err_text}"
                            self.result_signal.emit(False, error_msg, {})
                else:
                    self.result_signal.emit(False, "File not found", {})

            elif self.action == "get_all_users":
                url = f"{API_BASE_URL}/api/users" 
                response = requests.get(url, timeout=3)
                if response.status_code == 200:
                    self.result_signal.emit(True, "User List Fetched", {"users": response.json()})
                else:
                    self.result_signal.emit(False, f"Fetch Users Failed: {response.status_code}", {})

        except requests.exceptions.ConnectionError:
             self.result_signal.emit(False, "Connection Refused (Server Offline?)", {})
        except Exception as e:
            self.result_signal.emit(False, str(e), {})

# ------------------- System Monitor Thread -------------------
class SystemMonitor(QThread):
    data_collected = pyqtSignal(dict)

    def __init__(self, interval=2):
        super().__init__()
        self.interval = interval
        self.running = True

    def run(self):
        while self.running:
            try:
                cpu_percent = psutil.cpu_percent()
                mem = psutil.virtual_memory()
                net_io = psutil.net_io_counters()

                procs = []
                try:
                    procs = list(psutil.process_iter(['name', 'cpu_percent', 'memory_info']))
                except Exception:
                    procs = []
                
                procs.sort(key=lambda p: p.info.get('cpu_percent') or 0, reverse=True)

                process_list = []
                app_stats = {}

                for proc in procs[:100]:
                    try:
                        p_info = proc.info
                        if not p_info: continue

                        proc_name = p_info.get('name') or "N/A"
                        cpu_p = p_info.get('cpu_percent') or 0.0
                        mem_info = p_info.get('memory_info')
                        
                        mem_rss = 0
                        if mem_info:
                            mem_rss = mem_info.rss

                        # --- FIX: Grouping logic for "App Monitoring" ---
                        if proc_name not in app_stats:
                            app_stats[proc_name] = {'count': 0, 'cpu': 0.0, 'mem_bytes': 0}
                        app_stats[proc_name]['count'] += 1
                        app_stats[proc_name]['cpu'] += cpu_p
                        app_stats[proc_name]['mem_bytes'] += mem_rss

                        if len(process_list) < 50:
                            mem_mb_str = f"{mem_rss / (1024 ** 2):.1f} MB"
                            io_read = "N/A"
                            io_write = "N/A"
                            try:
                                io_counters = proc.io_counters()
                                io_read = f"{io_counters.read_bytes / (1024 * 1024):.1f} MB"
                                io_write = f"{io_counters.write_bytes / (1024 * 1024):.1f} MB"
                            except (psutil.AccessDenied, psutil.NoSuchProcess, AttributeError):
                                pass

                            process_list.append({
                                'name': proc_name,
                                'pid': proc.pid,
                                'cpu': f"{cpu_p:.1f}%",
                                'memory_mb': mem_mb_str,
                                'memory_rss': mem_rss, 
                                'io_read': io_read, 
                                'io_write': io_write
                            })
                    except (psutil.NoSuchProcess, psutil.AccessDenied):
                        continue
                
                sorted_apps = sorted(app_stats.items(), key=lambda item: item[1]['cpu'], reverse=True)[:10]
                top_apps = []
                for name, stats in sorted_apps:
                    top_apps.append({
                        'name': name,
                        'count': stats['count'],
                        'total_cpu': f"{stats['cpu']:.1f}%",
                        'total_mem': f"{stats['mem_bytes'] / (1024**2):.1f} MB"
                    })

                partitions = []
                try:
                    for part in psutil.disk_partitions(all=False):
                        try:
                            usage = psutil.disk_usage(part.mountpoint)
                            partitions.append({
                                'device': part.device,
                                'mountpoint': part.mountpoint,
                                'fstype': part.fstype,
                                'total_gb': f"{usage.total / (1024 ** 3):.1f} GB",
                                'used_gb': f"{usage.used / (1024 ** 3):.1f} GB",
                                'percent': usage.percent
                            })
                        except (psutil.AccessDenied, OSError):
                            continue
                except Exception:
                    pass

                cpu_emission = cpu_percent * 0.05 / 100
                memory_emission = mem.percent * 0.01 / 100
                network_emission = (net_io.bytes_sent + net_io.bytes_recv) / (1024**3) * 0.06
                
                data = {
                    'system': {
                        'cpu_percent': cpu_percent,
                        'cpu_count': psutil.cpu_count(),
                        'memory_percent': mem.percent,
                        'memory_used_gb': mem.used / (1024 **3),
                        'memory_total_gb': mem.total / (1024 **3),
                        'bytes_sent': net_io.bytes_sent,
                        'bytes_recv': net_io.bytes_recv,
                    },
                    'emissions': {
                        'cpu': cpu_emission, 
                        'memory': memory_emission,
                        'network': network_emission, 
                        'total': (cpu_emission + memory_emission + network_emission) * 1.05
                    },
                    'processes': process_list,
                    'top_apps': top_apps,
                    'filesystems': partitions,
                    'timestamp': datetime.now().isoformat()
                }
                self.data_collected.emit(data)
            except Exception as e:
                print(f"System monitor error: {e}")
            self.msleep(self.interval * 1000)

    def stop(self):
        self.running = False
        self.wait()

# ------------------- Browser Monitor -------------------
class BrowserMonitor(QThread):
    browser_data = pyqtSignal(dict)
    log_msg = pyqtSignal(str) 

    def __init__(self, interval=3):
        super().__init__()
        self.interval = interval
        self.running = True

    def run(self):
        self.log_msg.emit("Browser Thread: Loop Started")
        while self.running:
            try:
                browser_signatures = {
                    'chrome': {'name': 'Google Chrome', 'keywords': ['chrome', 'google-chrome']},
                    'brave': {'name': 'Brave Browser', 'keywords': ['brave']},
                    'firefox': {'name': 'Mozilla Firefox', 'keywords': ['firefox', 'firefox-bin']},
                    'msedge': {'name': 'Microsoft Edge', 'keywords': ['msedge', 'edge']},
                    'opera': {'name': 'Opera', 'keywords': ['opera']},
                    'safari': {'name': 'Safari', 'keywords': ['safari']},
                    'chromium': {'name': 'Chromium', 'keywords': ['chromium']}
                }
                stats = {key: {'name': val['name'], 'count': 0, 'cpu': 0.0, 'mem_mb': 0.0} 
                         for key, val in browser_signatures.items()}

                for proc in psutil.process_iter(['name', 'cpu_percent', 'memory_info']):
                    try:
                        p_info = proc.info
                        if not p_info or not p_info.get('name'): continue
                        
                        p_name = p_info['name'].lower()
                        for b_key, b_val in browser_signatures.items():
                            if any(k in p_name for k in b_val['keywords']):
                                stats[b_key]['count'] += 1
                                stats[b_key]['cpu'] += p_info.get('cpu_percent') or 0.0
                                mem_info = p_info.get('memory_info')
                                if mem_info:
                                    stats[b_key]['mem_mb'] += mem_info.rss / (1024 * 1024)
                                break 
                    except (psutil.NoSuchProcess, psutil.AccessDenied):
                        continue

                active_browsers = {k: v for k, v in stats.items() if v['count'] > 0}
                
                recommendations = []
                total_browser_mem = sum(b['mem_mb'] for b in active_browsers.values())
                
                if total_browser_mem > 1500: 
                    recommendations.append({
                        'type':'memory', 'severity':'warning',
                        'message':f"⚠️ High Browser Memory: {total_browser_mem/1024:.1f} GB total.",
                        'suggestion':'Close unused tabs.'
                    })
                
                for b_key, b_data in active_browsers.items():
                    if b_data['cpu'] > 15.0:
                         recommendations.append({
                            'type':'cpu', 'severity':'critical',
                            'message':f"🔴 {b_data['name']} is using high CPU ({b_data['cpu']:.1f}%).",
                            'suggestion':'Check for stuck pages.'
                        })

                if self.running:
                    self.browser_data.emit({'browsers': active_browsers, 'recommendations': recommendations})

            except Exception as e:
                self.log_msg.emit(f"Browser Monitor Error: {str(e)}")

            for _ in range(int(self.interval * 2)):
                if not self.running: break
                self.msleep(500)
        
        self.log_msg.emit("Browser Thread: Loop Ended")

    def stop(self):
        self.running = False
        self.wait()

# ------------------- Main GUI -------------------
class CarbonTradeWidget(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("CarbonTrade System Monitor & API Client")
        self.setMinimumSize(600, 500) 
        
        self.system_monitor = None
        self.browser_monitor = None
        
        self.worker = None      # General worker (register/upload)
        self.sync_worker = None # Points sync worker
        
        self.cer_balance = 0.0 
        self.accumulated_points_buffer = 0
        self.data_history = []
        self.user_map = {} 
        
        self.apply_style()
        self.init_ui()
        self.initialize_ui_placeholders()
        
        self.fetch_market_price()
        self.fetch_all_users()
        
        self.sync_timer = QTimer()
        self.sync_timer.timeout.connect(self.sync_points_to_server)
        self.sync_timer.start(10000) 

    def apply_style(self):
        self.setStyleSheet(DARK_MODE_STYLESHEET)
    
    def wrap_in_scroll(self, widget):
        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll.setWidget(widget)
        return scroll

    def init_ui(self):
        self.balance_label = QLabel("Local Balance: 0.0000 CER")
        self.balance_label.setFont(QFont("Arial", 18, QFont.Weight.Bold))
        self.balance_label.setStyleSheet("color: #4e9a06;")
        
        self.cpu_label = QLabel("CPU: --")
        self.cpu_progress = QProgressBar()
        
        self.memory_label = QLabel("Memory: --")
        self.memory_progress = QProgressBar()
        
        self.emissions_label = QLabel("Estimated CO2: --")
        self.emissions_label.setStyleSheet("color: #ff8c00; font-weight: bold;")

        self.price_label = QLabel("Market Price: --")
        self.price_label.setFont(QFont("Arial", 14))
        self.price_label.setStyleSheet("color: #729fcf;")
        
        self.api_status_label = QLabel("API Status: Initializing...")
        
        central = QWidget()
        self.setCentralWidget(central)
        layout = QVBoxLayout(central)

        # Header
        header_widget = QWidget()
        header_layout = QHBoxLayout(header_widget)
        self.resources_label = QLabel("Resources")
        self.resources_label.setFont(QFont("Arial", 12, QFont.Weight.Bold))
        self.resources_label.setStyleSheet("color: #4e9a06;")
        header_layout.addWidget(self.resources_label)
        header_layout.addStretch()
        layout.addWidget(header_widget)

        # Tabs with Scroll Areas
        self.tabs = QTabWidget()
        self.tabs.addTab(self.wrap_in_scroll(self.create_monitor_tab()), "System Monitor")
        self.tabs.addTab(self.wrap_in_scroll(self.create_browser_tab()), "Browser Efficiency") 
        self.tabs.addTab(self.wrap_in_scroll(self.create_processes_tab()), "Processes List")
        self.tabs.addTab(self.wrap_in_scroll(self.create_filesystems_tab()), "File Systems")
        self.tabs.addTab(self.wrap_in_scroll(self.create_api_tab()), "Account") 
        
        layout.addWidget(self.tabs)

        # Console
        console_group = QGroupBox("Console")
        console_layout = QVBoxLayout()
        self.console = QTextEdit()
        self.console.setReadOnly(True)
        self.console.setMaximumHeight(150)
        console_layout.addWidget(self.console)
        console_group.setLayout(console_layout)
        layout.addWidget(console_group)
        
        self.log(f"Application started on {platform.system()}.")

    # --- FIX: closeEvent to stop threads safely ---
    def closeEvent(self, event):
        self.log("Stopping threads for shutdown...")
        if self.system_monitor and self.system_monitor.isRunning():
            self.system_monitor.stop()
        if self.browser_monitor and self.browser_monitor.isRunning():
            self.browser_monitor.stop()
        event.accept()

    def initialize_ui_placeholders(self):
        self.cpu_label.setText("CPU: --")
        self.cpu_progress.setValue(0)
        self.memory_label.setText("Memory: --")
        self.memory_progress.setValue(0)
        self.emissions_label.setText("Estimated CO2: --")
        self.balance_label.setText(f"Local Balance: {self.cer_balance:.4f} CER")
        self.price_label.setText("Market Price: Connecting...")
        self.api_status_label.setText("API Status: Initializing...")
        
        if hasattr(self, 'browser_stats_display'):
            self.browser_stats_display.setText("Monitoring paused.")
            self.recommendations_list.clear()

    # ------------------- TAB: ACCOUNT -------------------
    def create_api_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        
        id_group = QGroupBox("Active User Selection (For Uploads)")
        id_layout = QHBoxLayout(id_group)
        id_lbl = QLabel("Select User:")
        
        self.user_dropdown = QComboBox()
        self.user_dropdown.setMinimumWidth(200)
        self.user_dropdown.currentIndexChanged.connect(self.update_current_user_selection)
        
        self.fetch_users_btn = QPushButton("Refresh User List")
        self.fetch_users_btn.clicked.connect(self.fetch_all_users)
        self.fetch_users_btn.setSizePolicy(QSizePolicy.Policy.Minimum, QSizePolicy.Policy.Fixed)

        id_layout.addWidget(id_lbl)
        id_layout.addWidget(self.user_dropdown)
        id_layout.addWidget(self.fetch_users_btn)
        layout.addWidget(id_group)
        
        market_group = QGroupBox("Market Data")
        market_layout = QVBoxLayout(market_group)
        self.refresh_price_btn = QPushButton("Refresh Price")
        self.refresh_price_btn.clicked.connect(self.fetch_market_price)
        market_layout.addWidget(self.price_label)
        market_layout.addWidget(self.refresh_price_btn)
        layout.addWidget(market_group)

        reg_group = QGroupBox("Register New User")
        reg_layout = QFormLayout()
        self.reg_name = QLineEdit()
        self.reg_email = QLineEdit()
        self.reg_pass = QLineEdit()
        self.reg_pass.setEchoMode(QLineEdit.EchoMode.Password)
        reg_layout.addRow("Name:", self.reg_name)
        reg_layout.addRow("Email:", self.reg_email)
        reg_layout.addRow("Password:", self.reg_pass)
        self.register_btn = QPushButton("Register")
        self.register_btn.clicked.connect(self.register_user)
        reg_cont = QWidget()
        reg_cont.setLayout(reg_layout)
        layout.addWidget(reg_group)
        reg_group.setLayout(QVBoxLayout())
        reg_group.layout().addWidget(reg_cont)
        reg_group.layout().addWidget(self.register_btn)

        mining_group = QGroupBox("Data Mining & Upload")
        mining_layout = QVBoxLayout(mining_group)
        self.csv_info_label = QLabel("Capture session history and upload to ML engine.")
        self.gen_csv_btn = QPushButton("Generate CSV & Upload")
        self.gen_csv_btn.setStyleSheet("background-color: #ff8c00; color: black; font-weight: bold;")
        self.gen_csv_btn.clicked.connect(self.generate_and_upload_csv)
        mining_layout.addWidget(self.csv_info_label)
        mining_layout.addWidget(self.gen_csv_btn)
        layout.addWidget(mining_group)

        balance_group = QGroupBox("Local Efficiency Wallet")
        balance_layout = QVBoxLayout(balance_group)
        balance_layout.addWidget(self.balance_label)
        balance_layout.addWidget(self.api_status_label)
        layout.addWidget(balance_group)

        layout.addStretch()
        return widget
    
    def update_current_user_selection(self, index):
        global CURRENT_SELECTED_USER_ID
        key = self.user_dropdown.currentText()
        if key in self.user_map:
            CURRENT_SELECTED_USER_ID = self.user_map[key]
            self.log(f"Active User set to: {key} (ID: {CURRENT_SELECTED_USER_ID})")

    def fetch_market_price(self):
        if self.worker and self.worker.isRunning():
            self.log("API busy. Please wait.")
            return

        self.api_status_label.setText("Status: Fetching Price...")
        self.worker = ApiWorker("get_market_price")
        self.worker.result_signal.connect(self.handle_api_result)
        self.worker.start()

    def fetch_all_users(self):
        if self.worker and self.worker.isRunning():
            self.log("API busy. Please wait.")
            return

        self.log("Fetching user list from API...")
        self.worker = ApiWorker("get_all_users")
        self.worker.result_signal.connect(self.handle_api_result)
        self.worker.start()

    def register_user(self):
        if self.worker and self.worker.isRunning():
            self.log("API busy. Please wait.")
            return

        name = self.reg_name.text()
        email = self.reg_email.text()
        password = self.reg_pass.text()
        if not name or not email or not password:
            QMessageBox.warning(self, "Error", "All fields required.")
            return
        payload = {"name": name, "email": email, "password": password}
        self.log(f"Registering: {email}")
        self.worker = ApiWorker("register_user", payload=payload)
        self.worker.result_signal.connect(self.handle_api_result)
        self.worker.start()

    def generate_and_upload_csv(self):
        if self.worker and self.worker.isRunning():
            self.log("API busy (Upload in progress?).")
            return

        if not self.data_history:
            QMessageBox.warning(self, "Error", "No history data collected. Start Monitoring first!")
            return
            
        filename = f"carbon_data_{int(datetime.now().timestamp())}.csv"
        try:
            with open(filename, mode='w', newline='') as file:
                writer = csv.writer(file)
                # --- FIX: Match EXACTLY what IngestionService.java expects ---
                writer.writerow(['userId', 'date', 'category', 'amount', 'unit', 'emissionFactor', 'cpu_percent', 'memory_percent', 'bytes_recv', 'emissions_kg'])
                
                for record in self.data_history:
                     # We use the computed emissions as 'amount' and set 'unit' to kg, 'ef' to 1.0
                     # This ensures backend stores the exact kgCO2e we calculated.
                     amount_co2 = record['co2'] 
                     
                     writer.writerow([
                         CURRENT_SELECTED_USER_ID, 
                         record['timestamp'][:10], 
                         "Computing",       # Category
                         amount_co2,        # Amount
                         "kg",              # Unit (required by backend)
                         1.0,               # Emission Factor (so amount * 1.0 = final co2)
                         record['cpu'], 
                         record['mem'], 
                         record['net'], 
                         record['co2']
                     ])
                     
            self.log(f"CSV Generated ({len(self.data_history)} rows). Uploading for User ID {CURRENT_SELECTED_USER_ID}...")
            self.worker = ApiWorker("upload_csv", payload={'userId': CURRENT_SELECTED_USER_ID}, file_path=filename)
            self.worker.result_signal.connect(self.handle_api_result)
            self.worker.start()
        except Exception as e:
            self.log(f"CSV Error: {e}")

    def sync_points_to_server(self):
        if self.accumulated_points_buffer < 1: return
        if self.sync_worker and self.sync_worker.isRunning():
            return

        payload = {"userId": CURRENT_SELECTED_USER_ID, "amount": int(self.accumulated_points_buffer), "reason": "efficiency_mining_autominer"}
        self.sync_worker = ApiWorker("earn_points", payload=payload)
        self.sync_worker.result_signal.connect(self.handle_api_result)
        self.sync_worker.start()

    def handle_api_result(self, success, message, data):
        if success:
            self.api_status_label.setText(f"Status: Success - {message}")
            self.log(f"API Success: {message}")
            
            if "pricePerCER" in data:
                self.price_label.setText(f"Market Price: {data['pricePerCER']} {data['currency']}")
            
            if message == "Registration Successful":
                self.log("Registration Complete. Refreshing User List...")
                QTimer.singleShot(500, self.fetch_all_users)

            if message == "User List Fetched":
                users = data.get("users", [])
                self.user_dropdown.blockSignals(True) 
                self.user_dropdown.clear()
                self.user_map = {}
                
                if isinstance(users, list):
                    for u in users:
                        uid = u.get('id')
                        name = u.get('name', 'Unknown')
                        display_text = f"{name} (ID: {uid})"
                        self.user_map[display_text] = uid
                        self.user_dropdown.addItem(display_text)
                    self.log(f"User list updated: {len(users)} users found.")
                else:
                    self.log("Invalid user list format.")
                self.user_dropdown.blockSignals(False)
                
                if self.user_dropdown.count() > 0:
                    self.user_dropdown.setCurrentIndex(0)
                    self.update_current_user_selection(0)

            if message == "Points Synced":
                self.accumulated_points_buffer = 0
            
            if message == "CSV Uploaded":
                self.log(f"Server response: {data}")
        else:
            self.api_status_label.setText(f"API Error: {message}")
            self.log(f"API Fail: {message}")

    # ------------------- TAB: SYSTEM MONITOR -------------------
    def create_monitor_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)

        cpu_group = QGroupBox("CPU Usage")
        cpu_layout = QVBoxLayout(cpu_group)
        cpu_layout.addWidget(self.cpu_label)
        cpu_layout.addWidget(self.cpu_progress)
        layout.addWidget(cpu_group)

        mem_group = QGroupBox("Memory")
        mem_layout = QVBoxLayout(mem_group)
        mem_layout.addWidget(self.memory_label)
        mem_layout.addWidget(self.memory_progress)
        layout.addWidget(mem_group)

        net_emissions_group = QGroupBox("Emissions")
        ne_layout = QVBoxLayout(net_emissions_group)
        ne_layout.addWidget(self.emissions_label)
        layout.addWidget(net_emissions_group)
        
        self.start_monitor_btn = QPushButton("Start Monitoring")
        self.start_monitor_btn.clicked.connect(self.start_monitoring)
        self.stop_monitor_btn = QPushButton("Stop Monitoring")
        self.stop_monitor_btn.clicked.connect(self.stop_monitoring)
        self.stop_monitor_btn.setEnabled(False)
        btn_layout = QHBoxLayout()
        btn_layout.addWidget(self.start_monitor_btn)
        btn_layout.addWidget(self.stop_monitor_btn)
        layout.addLayout(btn_layout)
        
        layout.addStretch()
        return widget

    def start_monitoring(self):
        # Privacy Concern: Ask usage permission
        reply = QMessageBox.question(
            self, 'Privacy Consent', 
            "CarbonTrade needs to scan your running processes to calculate efficiency scores.\n\nDo you allow this application to read your process list?",
            QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No, 
            QMessageBox.StandardButton.No
        )

        if reply == QMessageBox.StandardButton.Yes:
            if not self.system_monitor:
                self.system_monitor = SystemMonitor(interval=1)
                self.system_monitor.data_collected.connect(self.update_monitor_display)
                self.system_monitor.start()
                self.start_browser_monitoring(auto_start=True) 
                self.start_monitor_btn.setEnabled(False)
                self.stop_monitor_btn.setEnabled(True)
                self.log("System monitoring started (User Consented).")
        else:
            self.log("Monitoring start cancelled by user (Privacy).")

    def stop_monitoring(self):
        if self.system_monitor:
            self.system_monitor.stop()
            self.system_monitor = None
            self.stop_browser_monitoring(auto_stop=True) 
            self.start_monitor_btn.setEnabled(True)
            self.stop_monitor_btn.setEnabled(False)
            self.log("System monitoring stopped.")

    def update_monitor_display(self, data):
        sys_info = data['system']
        emissions = data['emissions']
        
        self.cpu_label.setText(f"CPU: {sys_info['cpu_percent']:.1f}%")
        self.cpu_progress.setValue(int(sys_info['cpu_percent']))
        
        self.memory_label.setText(f"Memory: {sys_info['memory_used_gb']:.2f} GB / {sys_info['memory_total_gb']:.2f} GB")
        self.memory_progress.setValue(int(sys_info['memory_percent']))
        
        self.emissions_label.setText(f"Est. CO2: {emissions['total']:.3f} kg")
        
        record = {
            'timestamp': data['timestamp'],
            'cpu': sys_info['cpu_percent'],
            'mem': sys_info['memory_percent'],
            'net': sys_info['bytes_recv'],
            'co2': emissions['total']
        }
        self.data_history.append(record)
        
        reward = (0.7 * (1.0 - (sys_info['cpu_percent']/100)) + 0.3 * (1.0 - (sys_info['memory_percent']/100))) * 0.5
        self.cer_balance += reward
        self.accumulated_points_buffer += reward 
        self.balance_label.setText(f"Local Balance: {self.cer_balance:.4f} CER")
        
        self.update_processes_display(data['processes'], data.get('top_apps', []))
        self.update_filesystems_display(data['filesystems'])

    # ------------------- TAB: BROWSER EFFICIENCY -------------------
    def create_browser_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        
        stats_group = QGroupBox("Detected Browser Usage (Real-Time)")
        stats_layout = QVBoxLayout(stats_group)
        self.browser_stats_display = QTextEdit()
        self.browser_stats_display.setReadOnly(True)
        stats_layout.addWidget(self.browser_stats_display)
        layout.addWidget(stats_group)

        rec_group = QGroupBox("Efficiency Recommendations")
        rec_layout = QVBoxLayout(rec_group)
        self.recommendations_list = QListWidget()
        rec_layout.addWidget(self.recommendations_list)
        layout.addWidget(rec_group)
        
        self.clean_browser_btn = QPushButton("Run Browser Optimization")
        self.clean_browser_btn.clicked.connect(self.show_optimization_dialog)
        
        self.start_browser_btn = QPushButton("Start Browser Monitor")
        # Direct connection to the function
        self.start_browser_btn.clicked.connect(self.start_browser_monitor_manual)
        
        self.stop_browser_btn = QPushButton("Stop Browser Monitor")
        self.stop_browser_btn.clicked.connect(lambda: self.stop_browser_monitoring(False))
        self.stop_browser_btn.setEnabled(False)
        
        btn_layout = QHBoxLayout()
        btn_layout.addWidget(self.clean_browser_btn)
        btn_layout.addWidget(self.start_browser_btn)
        btn_layout.addWidget(self.stop_browser_btn)
        layout.addLayout(btn_layout)
        
        layout.addStretch()
        return widget

    def start_browser_monitor_manual(self):
        self.log("Manual Start clicked.")
        self.start_browser_monitoring(auto_start=False)

    def start_browser_monitoring(self, auto_start=False):
        if not auto_start:  # Only ask when manually triggered
            reply = QMessageBox.question(
                self, 'Privacy Consent',
                "The Browser Monitor checks running processes to identify open web browsers and estimate memory usage.\n\nDo you want to allow this?",
                QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No,
                QMessageBox.StandardButton.No
            )
            if reply == QMessageBox.StandardButton.No:
                self.log("User denied browser monitoring consent.")
                return

        # Force Clean Start
        if self.browser_monitor is not None:
            self.log("Restarting Browser Monitor...")
            self.browser_monitor.stop()
            self.browser_monitor.wait()
            self.browser_monitor = None
            
        self.browser_monitor = BrowserMonitor(interval=2)
        self.browser_monitor.browser_data.connect(self.update_browser_display)
        self.browser_monitor.log_msg.connect(self.log) 
        self.browser_monitor.start()
        
        if not auto_start:
            self.start_browser_btn.setEnabled(False)
            self.stop_browser_btn.setEnabled(True)
            self.log("Browser monitoring started.")

    def stop_browser_monitoring(self, auto_stop=False):
        if self.browser_monitor:
            self.browser_monitor.stop()
            self.browser_monitor = None
            if not auto_stop:
                self.start_browser_btn.setEnabled(True)
                self.stop_browser_btn.setEnabled(False)
                self.browser_stats_display.setText("Monitoring Stopped.")
                self.recommendations_list.clear()
                self.log("Browser monitoring stopped.")

    def update_browser_display(self, data):
        browsers = data['browsers']
        self.browser_stats_display.clear()
        if not browsers:
            self.browser_stats_display.setText("No major web browsers detected running.\n(Scanning for: Chrome, Firefox, Brave, Edge, Opera)")
        else:
            self.browser_stats_display.append(f"{'BROWSER':<20} | {'PROCS':<5} | {'CPU %':<7} | {'MEM (MB)':<10}")
            self.browser_stats_display.append("-" * 60)
            for key, b in browsers.items():
                line = f"{b['name']:<20} | {b['count']:<5} | {b['cpu']:<7.1f} | {b['mem_mb']:<10.1f}"
                self.browser_stats_display.append(line)
        
        self.recommendations_list.clear()
        for r in data['recommendations']:
            item = QListWidgetItem(r['message'])
            color = '#ef2929' if r['severity']=='critical' else '#f57900'
            item.setForeground(QColor(color))
            self.recommendations_list.addItem(item)

    def show_optimization_dialog(self):
        if not self.browser_monitor:
            self.log("Start monitoring first.")
            return
        dialog = QDialog(self)
        dialog.setWindowTitle("Choose Optimization Strategy")
        layout = QVBoxLayout(dialog)
        term_btn = QPushButton("1. Terminate High-Memory Processes (>500MB)")
        term_btn.setStyleSheet("background-color: #ef2929; color: white;")
        term_btn.clicked.connect(lambda: [dialog.accept(), self.clean_browsers_terminate()])
        layout.addWidget(term_btn)
        prompt_btn = QPushButton("2. AI Prompt Advice")
        prompt_btn.clicked.connect(lambda: [dialog.accept(), self.show_ai_optimization_advice()])
        layout.addWidget(prompt_btn)
        dialog.exec()

    def clean_browsers_terminate(self):
        target_names = ['chrome', 'chromium', 'firefox', 'brave', 'opera', 'msedge', 'safari']
        killed = 0
        for proc in psutil.process_iter(['name', 'memory_info', 'pid']):
            try:
                if any(t in proc.info['name'].lower() for t in target_names) and proc.info['memory_info'].rss > 500*1024*1024:
                    psutil.Process(proc.info['pid']).kill()
                    killed += 1
            except: continue
        QMessageBox.information(self, "Result", f"Terminated {killed} processes.")

    def show_ai_optimization_advice(self):
        QMessageBox.information(self, "AI Tips", "1. Be Specific\n2. Limit Length\n3. Close Idle Tabs")

    # ------------------- TAB: PROCESSES & FS -------------------
    def create_processes_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        
        # --- NEW: App Monitoring Summary Section ---
        app_group = QGroupBox("Top App Impact (Aggregated)")
        app_layout = QVBoxLayout(app_group)
        self.app_table = QTableWidget()
        self.app_table.setColumnCount(4)
        self.app_table.setHorizontalHeaderLabels(["App Name", "Instances", "Total CPU", "Total Mem"])
        self.app_table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeMode.Stretch)
        self.app_table.setMaximumHeight(150) # Keep it small to leave room for process list
        app_layout.addWidget(self.app_table)
        layout.addWidget(app_group)
        
        # --- Existing Process List ---
        proc_group = QGroupBox("Detailed Processes")
        proc_layout = QVBoxLayout(proc_group)
        self.process_table = QTableWidget()
        self.process_table.setColumnCount(6)
        self.process_table.setHorizontalHeaderLabels(["Name", "ID", "CPU", "MEM", "READ", "WRITE"])
        self.process_table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeMode.Stretch)
        proc_layout.addWidget(self.process_table)
        
        self.kill_process_btn = QPushButton("Kill Selected Process")
        self.kill_process_btn.clicked.connect(self.kill_processes)
        proc_layout.addWidget(self.kill_process_btn)
        
        layout.addWidget(proc_group)
        return widget

    def update_processes_display(self, process_list, top_apps):
        # Update Individual Process List
        self.process_table.setRowCount(len(process_list[:20]))
        for row, proc in enumerate(process_list[:20]):
            self.process_table.setItem(row, 0, QTableWidgetItem(proc['name']))
            self.process_table.setItem(row, 1, QTableWidgetItem(str(proc['pid'])))
            self.process_table.setItem(row, 2, QTableWidgetItem(proc['cpu']))
            self.process_table.setItem(row, 3, QTableWidgetItem(proc['memory_mb']))
            self.process_table.setItem(row, 4, QTableWidgetItem(proc['io_read']))
            self.process_table.setItem(row, 5, QTableWidgetItem(proc['io_write']))
            
        # Update App Monitoring Summary
        self.app_table.setRowCount(len(top_apps))
        for row, app in enumerate(top_apps):
            self.app_table.setItem(row, 0, QTableWidgetItem(app['name']))
            self.app_table.setItem(row, 1, QTableWidgetItem(str(app['count'])))
            self.app_table.setItem(row, 2, QTableWidgetItem(app['total_cpu']))
            self.app_table.setItem(row, 3, QTableWidgetItem(app['total_mem']))

    def kill_processes(self):
        QMessageBox.information(self, "Info", "Select a process to kill (Feature implemented in full version)")

    def create_filesystems_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        self.fs_table = QTableWidget()
        self.fs_table.setColumnCount(5)
        self.fs_table.setHorizontalHeaderLabels(["Device", "Mount", "Type", "Total", "Used"])
        self.fs_table.horizontalHeader().setSectionResizeMode(1, QHeaderView.ResizeMode.Stretch)
        layout.addWidget(self.fs_table)
        return widget

    def update_filesystems_display(self, partitions):
        self.fs_table.setRowCount(len(partitions))
        for row, part in enumerate(partitions):
            self.fs_table.setItem(row, 0, QTableWidgetItem(part['device']))
            self.fs_table.setItem(row, 1, QTableWidgetItem(part['mountpoint']))
            self.fs_table.setItem(row, 2, QTableWidgetItem(part['fstype']))
            self.fs_table.setItem(row, 3, QTableWidgetItem(part['total_gb']))
            self.fs_table.setItem(row, 4, QTableWidgetItem(f"{part['used_gb']} ({part['percent']}%)"))

    def log(self, msg):
        self.console.append(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}")

# ------------------- Main -------------------
def main():
    app = QApplication(sys.argv)
    window = CarbonTradeWidget()
    window.show()
    sys.exit(app.exec())

if __name__=="__main__":
    main()