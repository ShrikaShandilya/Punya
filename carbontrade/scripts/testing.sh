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
# MARKET API
################################################
test_endpoint "Market: Get CER price" "200" \
  "$BASE/api/market/price"


################################################
# USER API
################################################
test_endpoint "User: Register" "200" \
  -X POST "$BASE/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"alice","email":"alice@test.com","password":"test123"}'

test_endpoint "User: Get profile" "200" \
  "$BASE/api/users/1/profile"

# Verify Security: Public User List should be BLOCKED (401 Unauthorized for anonymous)
test_endpoint "Security: List Users (Protected)" "401" \
  "$BASE/api/users"


################################################
# MINING API
################################################
test_endpoint "Mining: Health Check" "200" \
  "$BASE/mining/health"

test_endpoint "Mining: Upload CSV" "200" \
  -F "file=@test_data.csv" "$BASE/mining/ingest"

test_endpoint "Mining: Analyze valid" "200" \
  "$BASE/mining/analyze?userId=1&start=2024-01-01&end=2024-12-31&k=4"

test_endpoint "Mining: Missing params (neg)" "400" \
  "$BASE/mining/analyze?userId=1"

test_endpoint "Mining: Missing file (neg)" "400" \
  -X POST "$BASE/mining/ingest"


################################################
# POINTS API  (correct matching of your backend)
################################################

# Get points rules
test_endpoint "Points: Rules" "200" \
  "$BASE/api/points/rules"

# Earn points (should auto-convert)
test_endpoint "Points: Earn points" "200" \
  -X POST "$BASE/api/points/earn" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"amount":75,"reason":"auto-test"}'

# Confirm balance
test_endpoint "Points: Get balance" "200" \
  "$BASE/api/points/balance?userId=1"


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
