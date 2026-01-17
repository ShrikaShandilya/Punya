import streamlit as st
import requests

st.set_page_config(page_title="Greencoin GUI", layout="wide")

def api_request(method, url, **kwargs):
    try:
        resp = requests.request(method, url, **kwargs)
        try:
            data = resp.json()
        except:
            data = resp.text
        return resp.ok, data, resp
    except Exception as e:
        return False, {"error": str(e)}, None

def metric(title, value, subtitle=""):
    st.markdown(f"### {title}")
    st.markdown(f"**{value}**")
    if subtitle:
        st.caption(subtitle)

st.sidebar.header("API Settings")
base_url = st.sidebar.text_input("API Base URL", value="http://localhost:8081")
debug = st.sidebar.checkbox("Developer Debug", value=False)

# Users
st.header("Users")

with st.expander("Register"):
    name = st.text_input("Name")
    email = st.text_input("Email")
    password = st.text_input("Password", type="password")
    if st.button("Register"):
        payload = {"name": name, "email": email, "password": password}
        ok, data, _ = api_request("POST", f"{base_url}/api/users/register", json=payload)
        st.write(data)
        if debug:
            st.json(data)

with st.expander("Login"):
    email_l = st.text_input("Email", key="login_email")
    password_l = st.text_input("Password", type="password", key="login_pass")
    if st.button("Login"):
        payload = {"email": email_l, "password": password_l}
        ok, data, _ = api_request("POST", f"{base_url}/api/users/login", json=payload)
        st.write(data)
        if debug:
            st.json(data)

with st.expander("Profile"):
    uid_raw = st.text_input("User ID (number)", key="profile_uid")
    if st.button("Fetch Profile"):
        if not uid_raw.strip().isdigit():
            st.warning("User ID must be numeric.")
        else:
            uid = int(uid_raw)
            ok, data, _ = api_request("GET", f"{base_url}/api/users/{uid}/profile")
            st.write(data)
            if debug:
                st.json(data)

# Points & Coins
st.header("Points & Coins")

with st.expander("Balance"):
    uid_bal = st.text_input("User ID", key="bal_uid")
    if st.button("Fetch Balance"):
        ok, data, _ = api_request("GET", f"{base_url}/api/points/balance", params={"userId": uid_bal})
        if ok:
            coins = int(data.get("coins", 0))
            points = int(data.get("points", 0))
            c1, c2, c3 = st.columns(3)
            with c1: metric("Coins", coins)
            with c2: metric("Points", points)
            with c3:
                need = (50 - (points % 50)) % 50
                metric("Next coin in", f"{need} pts")
        st.write(data)
        if debug:
            st.json(data)

with st.expander("Adjust Points"):
    uid_adj = st.text_input("User ID", key="adj_uid")
    amount = st.number_input("Point change (negative to deduct)", min_value=-10000, max_value=10000, step=1, value=10)
    reason = st.text_input("Reason", value="adjustment")
    if st.button("Apply Adjustment"):
        payload = {"userId": int(uid_adj) if uid_adj else None, "amount": int(amount), "reason": reason}
        ok, data, _ = api_request("POST", f"{base_url}/api/points/earn", json=payload)
        st.write(data)
        if debug:
            st.json(data)

        # refresh
        ok2, data2, _ = api_request("GET", f"{base_url}/api/points/balance", params={"userId": uid_adj})
        if ok2:
            coins = int(data2.get("coins", 0))
            points = int(data2.get("points", 0))
            c1, c2, c3 = st.columns(3)
            with c1: metric("Coins", coins)
            with c2: metric("Points", points)
            with c3:
                need = (50 - (points % 50)) % 50
                metric("Next coin in", f"{need} pts")
        if debug:
            st.json(data2)


# Mining
st.header("Mining Data")

with st.expander("Upload ingestion CSV"):
    uploaded = st.file_uploader("Upload CSV", type=["csv"])
    if uploaded and st.button("Ingest"):
        files = {"file": ("data.csv", uploaded.read(), "text/csv")}
        ok, data, _ = api_request("POST", f"{base_url}/mining/ingest", files=files)
        st.write(data)
        if debug:
            st.json(data)

with st.expander("Analyze"):
    uid_an = st.text_input("User ID", key="an_uid")
    if st.button("Run Analysis"):
        ok, data, _ = api_request("GET", f"{base_url}/mining/analyze", params={"userId": uid_an})
        st.write(data)
        if debug:
            st.json(data)
