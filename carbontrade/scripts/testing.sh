#!/bin/bash

echo "===================================="
echo " CarbonTrade API – Full Test Suite"
echo "===================================="
echo

BASE="http://localhost:8081"
PASSED=0
FAILED=0
FAILED_TESTS=()

#############################################
# Utility: Test endpoint
#############################################
function test_endpoint() {
  local name="$1"
  local expected_status="$2"
  shift 2

  local response=$(curl -s -w "\n%{http_code}" "$@")
  local status=$(echo "$response" | tail -n1)
  local body=$(echo "$response" | head -n-1)

  if [ "$status" = "$expected_status" ]; then
    echo "✓ PASS: $name"
    if command -v jq >/dev/null 2>&1; then
      echo "  Response: $(echo "$body" | jq -c '.' 2>/dev/null || echo "$body" | head -c150)"
    else
      echo "  Response: $(echo "$body" | head -c150)"
    fi
    ((PASSED++))
  else
    echo "✗ FAIL: $name (Expected $expected_status, Got $status)"
    if command -v jq >/dev/null 2>&1; then
      echo "  Response: $(echo "$body" | jq -c '.' 2>/dev/null || echo "$body" | head -c150)"
    else
      echo "  Response: $(echo "$body" | head -c150)"
    fi
    FAILED_TESTS+=("$name|$status|$body")
    ((FAILED++))
  fi
}

################################################
# AUTH API (NEW)
################################################
# Register a user
# Note: Using a random username to avoid collisions on repeated runs
SUFFIX=$((RANDOM % 1000))
USERNAME="trader$SUFFIX"
EMAIL="trader$SUFFIX@test.com"

test_endpoint "Auth: Register" "200" \
  -X POST "$BASE/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"test1234\"}"

# Login to get JWT token
LOGIN_RESPONSE=$(curl -s -X POST "$BASE/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"test1234\"}")

# Extract Token from JSON ({"accessToken":"...", "tokenType":"Bearer"})
if command -v jq >/dev/null 2>&1; then
  TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
else
  # Fallback sed extraction if jq is missing
  TOKEN=$(echo "$LOGIN_RESPONSE" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
fi

echo "JWT Token: ${TOKEN:0:20}..."

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo "✗ FAIL: Failed to obtain token"
  echo "  Response: $LOGIN_RESPONSE"
  exit 1
fi

# Verify token
test_endpoint "Auth: Verify token" "200" \
  "$BASE/api/v1/auth/verify?token=$TOKEN"

################################################
# USER API
################################################

# Get user profile (should succeed if logged in)
# We need the ID. For now assuming we can fetch it or just test the endpoint existence.
# The user might be ID 1 if it's the first run, or higher. 
# Since we don't know the ID easily without an endpoint returning "me", 
# we will try to just hit the KYC status endpoint which uses the token to find user.

test_endpoint "User: Get KYC Status" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/v1/users/kyc/status"

################################################
# KYC API
################################################
# Create dummy file
echo "dummy passport content" > dummy_passport.txt

test_endpoint "KYC: Upload Document" "200" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@dummy_passport.txt" \
  "$BASE/api/v1/users/kyc/upload"

# Clean up
rm dummy_passport.txt

# Verify status updated
test_endpoint "KYC: Verify Status Updated" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/v1/users/kyc/status"

################################################
# SECURITY CHECKS
################################################
# Protected endpoint without token should fail
test_endpoint "Security: Unauthorized Access" "403" \
  "$BASE/api/v1/users/kyc/status"

################################################
# MINING API
################################################
test_endpoint "Mining: Health Check" "200" \
  "$BASE/mining/health"

# Upload CSV (replace with a test CSV path if available, or skip if file missing)
if [ -f "test_data.csv" ]; then
  test_endpoint "Mining: Upload CSV" "200" \
    -F "file=@test_data.csv" "$BASE/mining/ingest"
else
  echo "⚠ SKIP: Mining Upload (test_data.csv not found)"
fi

################################################
# CARBON API
################################################
test_endpoint "Carbon: Info" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/carbon/info"

test_endpoint "Carbon: Calculate" "200" \
  -X POST "$BASE/api/carbon/calculate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"electricity": 100, "travel": 50, "dietType": 2}'

################################################
# POINTS API
################################################
test_endpoint "Points: Rules" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/points/rules"

# Earn points (Need userId, let's assume we can get it from balance or just use ID 1)
# Since we don't have a reliable way to get our own ID from the token in this script easily without 'me' endpoint,
# we will skip specific ID-based tests that rely on foreign keys if we don't have the ID.
# However, for now, let's try with ID 1 as it likely exists or will be created.
TEST_USER_ID=1

test_endpoint "Points: Earn" "200" \
  -X POST "$BASE/api/points/earn" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\": $TEST_USER_ID, \"amount\": 100, \"reason\": \"bonus\"}"

test_endpoint "Points: Balance" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/points/balance?userId=$TEST_USER_ID"

################################################
# MARKET API
################################################
test_endpoint "Market: Get Price" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/market/price"

# Buy CER
test_endpoint "Market: Buy CER" "200" \
  -X POST "$BASE/api/market/buy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\": $TEST_USER_ID, \"amount\": 10.0}"

# Sell CER
test_endpoint "Market: Sell CER" "200" \
  -X POST "$BASE/api/market/sell" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\": $TEST_USER_ID, \"amount\": 5.0}"

################################################
# TRANSACTIONS API
################################################
test_endpoint "Transactions: Get All" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/transactions/all"

test_endpoint "Transactions: Get User Transactions" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/transactions/user/$TEST_USER_ID"

# We can't easily predict a valid Transaction ID without parsing previous responses, 
# so we'll skip the specific GET by ID or try ID 1.
test_endpoint "Transactions: Get Transaction 1" "200" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/transactions/1"

################################################
# SUMMARY
################################################
echo
echo "===================================="
echo " Test Summary"
echo "===================================="
echo "Total: $((PASSED + FAILED)) | Passed: $PASSED | Failed: $FAILED"
echo

if [ $FAILED -gt 0 ]; then
  echo "Failed Tests Details:"
  echo "------------------------------------"
  for test_info in "${FAILED_TESTS[@]}"; do
    IFS='|' read -r name status body <<< "$test_info"
    echo
    echo "Test: $name"
    echo "Status: $status"
    echo -n "Response: "
    if command -v jq >/dev/null 2>&1; then
      echo "$body" | jq -r '.message // .error // .details // .' 2>/dev/null || echo "$body" | head -c200
    else
      echo "$body" | head -c200
    fi
  done
  echo
  exit 1
else
  echo "All tests passed! ✓"
  exit 0
fi

