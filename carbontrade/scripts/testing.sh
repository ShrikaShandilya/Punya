#!/bin/bash

echo "===================================="
echo " CarbonTrade API – Full Test Suite"
echo "===================================="
echo

BASE="http://localhost:8080"
PASSED=0
FAILED=0
FAILED_TESTS=()

function test_endpoint() {
  local name="$1"
  local expected_status="$2"
  shift 2
  local response=$(curl -s -w "\n%{http_code}" "$@")
  local status=$(echo "$response" | tail -n1)
  local body=$(echo "$response" | head -n-1)
  
  if [ "$status" = "$expected_status" ]; then
    echo "✓ PASS: $name"
    echo "  Response: $(echo "$body" | jq -c '.' 2>/dev/null || echo "$body" | head -c 150)"
    ((PASSED++))
  else
    echo "✗ FAIL: $name (Expected $expected_status, Got $status)"
    echo "  Response: $(echo "$body" | jq -c '.' 2>/dev/null || echo "$body" | head -c 150)"
    ((FAILED++))
    FAILED_TESTS+=("$name|$status|$body")
  fi
}

# Market API
test_endpoint "Market: Get current CER price" "200" "$BASE/api/market/price"

# User API
test_endpoint "User: Register" "200" -X POST "$BASE/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"alice","email":"alice@test.com","password":"test123"}'

test_endpoint "User: Get profile" "200" "$BASE/api/users/1/profile"

# Mining API
test_endpoint "Mining: Health check" "200" "$BASE/mining/health"

test_endpoint "Mining: Upload CSV" "200" -F "file=@test_data.csv" "$BASE/mining/ingest"

test_endpoint "Mining: Analyze data" "200" "$BASE/mining/analyze?userId=1&start=2024-01-01&end=2024-12-31&k=4"

test_endpoint "Mining: Missing parameters (negative)" "400" "$BASE/mining/analyze?userId=1"

test_endpoint "Mining: Missing file (negative)" "400" -X POST "$BASE/mining/ingest"

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
    echo "Response: $(echo "$body" | jq -r '.message // .error // .' 2>/dev/null || echo "$body" | head -c 200)"
  done
  echo
  exit 1
else
  echo "All tests passed! ✓"
  exit 0
fi

