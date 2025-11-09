from __future__ import annotations
import json
from typing import Any, Dict, Tuple

import pandas as pd
import requests
import streamlit as st

# -------------------------
# Page config & styling
# -------------------------
st.set_page_config(
    page_title="CarbonTrade Console",
    page_icon="🌿",
    layout="wide",
    initial_sidebar_state="expanded",
)

CUSTOM_CSS = """
<style>
:root {
  --ink: #e5e7eb;       /* slate-200 */
  --muted: #94a3b8;     /* slate-400 */
  --bg: #0b1220;        /* deep */
  --card: #0f172a;      /* slate-900 */
  --brand: #22c55e;     /* green-500 */
  --brand2: #16a34a;    /* green-600 */
  --accent: #38bdf8;    /* sky-400 */
}
html, body, [class*="css"] {
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;
}
.main > div {padding-top: 0 !important;}
/* Headings */
h1, h2, h3, h4 { color: var(--ink); }
p, label, span, div { color: var(--ink); }
/* Cards */
.ct-card {
  background: linear-gradient(180deg, rgba(34,197,94,.06), rgba(56,189,248,.05));
  border: 1px solid rgba(148,163,184,.18);
  border-radius: 16px;
  padding: 1rem 1.25rem;
  margin-bottom: 1rem;
}
.ct-title { font-weight: 700; letter-spacing: .3px; color: var(--brand); }
.ct-micro { color: var(--muted); font-size: .85rem; }
.ct-kpi { font-size: 2rem; font-weight: 800; color: var(--ink); }
.stButton>button {
  border-radius: 10px;
  padding: .5rem 1rem;
  font-weight: 600;
}
/* Hide Streamlit footer & menu for a cleaner kiosk feel */
#MainMenu {visibility: hidden;}
footer {visibility: hidden;}
</style>
"""
st.markdown(CUSTOM_CSS, unsafe_allow_html=True)

# -------------------------
# Helpers
# -------------------------
def api_request(method: str, url: str, *, params=None, json_body=None, files=None, headers=None) -> Tuple[bool, Any, int]:
    try:
        resp = requests.request(method, url, params=params, json=json_body, files=files, headers=headers, timeout=30)
        data: Any
        if resp.headers.get("content-type","").startswith("application/json"):
            data = resp.json()
        else:
            data = {"raw": resp.text}
        return resp.ok, data, resp.status_code
    except Exception as e:
        return False, {"exception": str(e)}, 0

def show_api_debug(ok: bool, data: Any, *, label: str):
    if st.session_state.get("debug"):
        with st.expander(f"API debug • {label}", expanded=False):
            st.code(json.dumps(data, indent=2, ensure_ascii=False), language="json")

def metric(label: str, value: Any, help_text: str | None = None):
    st.markdown(f"<div class='ct-card'><div class='ct-title'>{label}</div>"
                f"<div class='ct-kpi'>{value}</div>"
                f"{f'<div class=\"ct-micro\">{help_text}</div>' if help_text else ''}</div>", unsafe_allow_html=True)

# -------------------------
# Sidebar
# -------------------------
with st.sidebar:
    st.title("🌿 CarbonTrade")
    base_url = st.text_input("API Base URL", value="http://localhost:8080", help="Spring Boot server (default dev)")
    st.toggle("Developer debug (show API payloads)", value=False, key="debug")
    st.divider()
    nav = st.radio("Navigate", ["Dashboard","Market","Users","Mining","Points"], index=0)
    st.caption("Run with:  streamlit run gui.py")

# -------------------------
# Dashboard
# -------------------------
if nav == "Dashboard":
    st.markdown("### Overview")
    c1, c2, c3 = st.columns(3)

    with c1:
        ok, data, code = api_request("GET", f"{base_url}/mining/health")
        metric("Service Health", "Healthy ✅" if ok else "Unavailable ❌", "Mining subsystem status")
        show_api_debug(ok, data, label="GET /mining/health")

    with c2:
        ok, data, code = api_request("GET", f"{base_url}/api/market/price")
        price = data.get("pricePerCER") if ok else "—"
        currency = data.get("currency", "") if ok else ""
        metric("CER Price", f"{price} {currency}".strip(), "Current trading price")
        show_api_debug(ok, data, label="GET /api/market/price")

    with c3:
        ok, data, code = api_request("GET", f"{base_url}/api/points/rules")
        rules_label = "Loaded" if ok else "N/A"
        metric("Reward Rules", rules_label, "Points → Coins conversion")
        show_api_debug(ok, data, label="GET /api/points/rules")

    st.markdown("---")
    st.subheader("Quick Actions")
    qa1, qa2 = st.columns(2)
    with qa1:
        if st.button("Refresh Health"):
            ok, data, _ = api_request("GET", f"{base_url}/mining/health")
            st.toast("Health checked.", icon="✅" if ok else "❌")
            show_api_debug(ok, data, label="refresh health")
    with qa2:
        if st.button("Refresh Price"):
            ok, data, _ = api_request("GET", f"{base_url}/api/market/price")
            st.toast("Price refreshed.", icon="💹")
            show_api_debug(ok, data, label="refresh price")

# -------------------------
# Market
# -------------------------
elif nav == "Market":
    st.header("Market")
    st.write("Live carbon credit pricing.")
    if st.button("Get current price"):
        ok, data, _ = api_request("GET", f"{base_url}/api/market/price")
        if ok:
            metric("CER Price", f"{data.get('pricePerCER')} {data.get('currency','')}")
        else:
            st.error("Couldn't fetch price.")
        show_api_debug(ok, data, label="GET /api/market/price")

# -------------------------
# Users
# -------------------------
elif nav == "Users":
    st.header("Users")

    with st.expander("Register"):
        name = st.text_input("Name", key="reg_name_b")
        email = st.text_input("Email", key="reg_email_b")
        pwd = st.text_input("Password", type="password", key="reg_pwd_b")
        if st.button("Create Account"):
            payload = {"name": name, "email": email, "password": pwd}
            ok, data, _ = api_request("POST", f"{base_url}/api/users/register", json_body=payload)
            if ok:
                st.success(f"Welcome, {name}! Account created.")
            else:
                st.error("Registration failed.")
            show_api_debug(ok, data, label="POST /api/users/register")

    with st.expander("Login"):
        email_l = st.text_input("Email", key="login_email_b")
        pwd_l = st.text_input("Password", type="password", key="login_pwd_b")
        if st.button("Sign in"):
            payload = {"email": email_l, "password": pwd_l}
            ok, data, _ = api_request("POST", f"{base_url}/api/users/login", json_body=payload)
            if ok:
                st.session_state["auth"] = data
                st.success("Logged in.")
            else:
                st.error("Invalid credentials.")
            show_api_debug(ok, data, label="POST /api/users/login")

    with st.expander("Profile"):
        uid = st.text_input("User ID", key="profile_uid_b")
        if st.button("Fetch Profile"):
            ok, data, _ = api_request("GET", f"{base_url}/api/users/{uid}/profile")
            if ok and isinstance(data, dict):
                left, right = st.columns(2)
                with left:
                    st.markdown("**Name**")
                    st.write(data.get("name","—"))
                    st.markdown("**Email**")
                    st.write(data.get("email","—"))
                with right:
                    st.markdown("**CER Balance**")
                    st.write(data.get("cerBalance","—"))
            else:
                st.error("Profile not found.")
            show_api_debug(ok, data, label="GET /api/users/{id}/profile")

# -------------------------
# Mining
# -------------------------
elif nav == "Mining":
    st.header("Analytics")
    st.caption("Upload CSV → cluster & detect anomalies → award points automatically.")

    c1, c2 = st.columns(2)
    with c1:
        if st.button("Health Check"):
            ok, data, _ = api_request("GET", f"{base_url}/mining/health")
            st.toast("Mining is healthy." if ok else "Mining not reachable.", icon="✅" if ok else "❌")
            show_api_debug(ok, data, label="GET /mining/health")

    st.markdown("---")
    st.subheader("Ingest CSV")
    up = st.file_uploader("Carbon footprint .csv", type=["csv"])
    auto = st.toggle("Auto-run Analyze after ingest", value=True)
    if st.button("Upload and Process"):
        if not up:
            st.warning("Please choose a CSV file.")
        else:
            files = {"file": (up.name, up.getvalue(), "text/csv")}
            ok, data, _ = api_request("POST", f"{base_url}/mining/ingest", files=files)
            if ok:
                st.success(f"Ingested {data.get('recordsProcessed','?')} records.")
                if auto:
                    st.info("Analyzing…")
                    ok2, data2, _ = api_request("GET", f"{base_url}/mining/analyze")
                    if ok2:
                        st.success(data2.get("summary","Analysis complete."))
                        # Show anomalies table (clean)
                        anomalies = data2.get("anomalies", [])
                        if anomalies:
                            st.markdown("**Anomalies**")
                            df = pd.DataFrame(anomalies)
                            st.dataframe(df, use_container_width=True, hide_index=True)
                        # Show compact cluster meta
                        clusters = data2.get("clusters", {})
                        if clusters:
                            meta = {k: v for k, v in clusters.items() if k in ("k","labels")}
                            st.markdown("**Cluster Overview**")
                            st.json(meta, expanded=False)
                    else:
                        st.error("Analyze failed.")
                    show_api_debug(ok2, data2, label="GET /mining/analyze")
            else:
                st.error("Ingest failed.")
            show_api_debug(ok, data, label="POST /mining/ingest")

    st.subheader("Analyze (manual)")
    if st.button("Run Analyze"):
        ok, data, _ = api_request("GET", f"{base_url}/mining/analyze")
        if ok:
            st.success(data.get("summary","Analysis complete."))
            anomalies = data.get("anomalies", [])
            if anomalies:
                df = pd.DataFrame(anomalies)
                st.dataframe(df, use_container_width=True, hide_index=True)
        else:
            st.error("Analyze failed.")
        show_api_debug(ok, data, label="GET /mining/analyze")

# -------------------------
# Points
# -------------------------
elif nav == "Points":
    st.header("Points & Coins")

    col1, col2 = st.columns(2)
    with col1:
        if st.button("Reward Rules"):
            ok, data, _ = api_request("GET", f"{base_url}/api/points/rules")
            if ok:
                st.success("Rules loaded.")
                st.json(data, expanded=False) if st.session_state.get("debug") else st.write("Conversion rules available.")
            else:
                st.error("Couldn't load rules.")
            show_api_debug(ok, data, label="GET /api/points/rules")

    st.markdown("---")
    st.subheader("Balance")
    uid = st.text_input("User ID", key="bal_uid_b")
    if st.button("Fetch Balance"):
        ok, data, _ = api_request("GET", f"{base_url}/api/points/balance", params={"userId": uid})
        if ok:
            coins = data.get("coins","—")
            points = data.get("points","—")
            cA, cB = st.columns(2)
            with cA: metric("Coins", coins)
            with cB: metric("Points", points)
        else:
            st.error("Failed to fetch balance.")
        show_api_debug(ok, data, label="GET /api/points/balance")

    st.markdown("---")
    st.subheader("Manual Earn (dev)")
    uid2 = st.text_input("User ID", key="earn_uid_b")
    amt = st.number_input("Amount", min_value=1, value=50, step=1)
    reason = st.text_input("Reason", value="ingestion_bonus")
    if st.button("Post Earn"):
        payload = {"userId": int(uid2) if uid2 else None, "amount": int(amt), "reason": reason}
        ok, data, _ = api_request("POST", f"{base_url}/api/points/earn", json_body=payload)
        st.success("Points awarded.") if ok else st.error("Earn failed.")
        show_api_debug(ok, data, label="POST /api/points/earn")