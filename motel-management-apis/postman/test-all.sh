#!/bin/bash

# MotelChain API - Complete Test Suite
# Handles existing data gracefully

echo "=========================================="
echo "  MotelChain API - Test Suite"
echo "=========================================="
echo ""

# Get existing record to use for update/delete tests
echo "📋 Fetching existing records..."
EXISTING=$(curl -s "http://localhost:8085/motelApi/v1/motelChains?page=0&size=1")
EXISTING_ID=$(echo "$EXISTING" | jq -r '.data.content[0].motelChainId // empty')

if [ -z "$EXISTING_ID" ]; then
    echo "⚠️  No existing records found. Will create one first."
    USE_EXISTING=false
else
    echo "✅ Found existing record: $EXISTING_ID"
    USE_EXISTING=true
fi
echo ""

# Test 1: Create Valid MotelChain
echo "====== Test 1: Create Valid MotelChain ======"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8085/motelApi/v1/motelChains \
  -H "Content-Type: application/json" \
  -d '{
    "motelChainName": "Marriott International",
    "displayName": "Marriott",
    "state": "Maryland",
    "pincode": "20817",
    "status": "ACTIVE",
    "address": {
      "addressLine1": "7750 Wisconsin Avenue",
      "addressLine2": "Building A",
      "landmark": "Near Metro Station",
      "addressName": "Corporate HQ",
      "status": "ACTIVE"
    },
    "contactInfo": {
      "phoneNumber": "+13015551000",
      "email": "info@marriott.com",
      "contactName": "Customer Service",
      "contactPosition": "Support Manager",
      "contactType": "PRIMARY",
      "contactDescription": "Main contact line",
      "status": "ACTIVE"
    }
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
    echo "✅ Status: $HTTP_CODE Created"
    echo "$BODY" | jq .
    NEW_ID=$(echo "$BODY" | jq -r '.data.motelChainId')
    TEST_ID="$NEW_ID"
    echo "📝 Saved ID for testing: $TEST_ID"
elif [ "$HTTP_CODE" = "409" ]; then
    echo "⚠️  Status: $HTTP_CODE Conflict (already exists)"
    echo "$BODY" | jq .
    if [ "$USE_EXISTING" = true ]; then
        TEST_ID="$EXISTING_ID"
        echo "📝 Using existing ID: $TEST_ID"
    fi
else
    echo "❌ Status: $HTTP_CODE"
    echo "$BODY" | jq .
fi
echo ""

# Test 2: Validation Errors
echo "====== Test 2: Validation Errors ======"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8085/motelApi/v1/motelChains \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Test",
    "state": "C",
    "pincode": "123",
    "status": "ACTIVE"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "400" ]; then
    echo "✅ Status: $HTTP_CODE Bad Request"
else
    echo "❌ Expected 400, got: $HTTP_CODE"
fi
echo "$BODY" | jq .
echo ""

# Test 3: Duplicate MotelChain
echo "====== Test 3: Duplicate MotelChain ======"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8085/motelApi/v1/motelChains \
  -H "Content-Type: application/json" \
  -d '{
    "motelChainName": "Marriott International",
    "displayName": "Marriott",
    "state": "Maryland",
    "pincode": "20817",
    "status": "ACTIVE"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "409" ]; then
    echo "✅ Status: $HTTP_CODE Conflict"
else
    echo "⚠️  Expected 409, got: $HTTP_CODE"
fi
echo "$BODY" | jq .
echo ""

# Test 4: Get All (Paginated)
echo "====== Test 4: Get All (Paginated) ======"
RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8085/motelApi/v1/motelChains?page=0&size=10&sort=createdAt&direction=desc")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Status: $HTTP_CODE OK"
    TOTAL=$(echo "$BODY" | jq -r '.data.pagination.total_elements')
    echo "📊 Total records: $TOTAL"
else
    echo "❌ Expected 200, got: $HTTP_CODE"
fi
echo "$BODY" | jq '.data.pagination'
echo ""

# Test 5: Get by ID
echo "====== Test 5: Get by ID ======"
if [ -n "$TEST_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8085/motelApi/v1/motelChains/$TEST_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ Status: $HTTP_CODE OK"
        echo "$BODY" | jq '{motelChainId, motelChainName, state, pincode}'
    else
        echo "❌ Expected 200, got: $HTTP_CODE"
        echo "$BODY" | jq .
    fi
else
    echo "⚠️  Skipped - No valid ID available"
fi
echo ""

# Test 6: Get Not Found
echo "====== Test 6: Get Not Found ======"
RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8085/motelApi/v1/motelChains/00000000-0000-0000-0000-000000000000")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "404" ]; then
    echo "✅ Status: $HTTP_CODE Not Found"
else
    echo "❌ Expected 404, got: $HTTP_CODE"
fi
echo "$BODY" | jq .
echo ""

# Test 7: Update MotelChain
echo "====== Test 7: Update MotelChain ======"
if [ -n "$TEST_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "http://localhost:8085/motelApi/v1/motelChains/$TEST_ID" \
      -H "Content-Type: application/json" \
      -d '{
        "motelChainName": "Marriott International Group",
        "displayName": "Marriott Worldwide",
        "state": "Maryland",
        "pincode": "20817",
        "status": "ACTIVE",
        "address": {
          "addressLine1": "7750 Wisconsin Avenue Updated",
          "addressLine2": "Building B",
          "landmark": "Near Convention Center",
          "addressName": "Global HQ",
          "status": "ACTIVE"
        },
        "contactInfo": {
          "phoneNumber": "+13015552000",
          "email": "support@marriott.com",
          "contactName": "Global Support",
          "contactPosition": "Director",
          "contactType": "PRIMARY",
          "contactDescription": "Updated contact",
          "status": "ACTIVE"
        }
      }')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ Status: $HTTP_CODE OK"
        echo "$BODY" | jq '{motelChainName, displayName, updatedAt}'
    else
        echo "❌ Expected 200, got: $HTTP_CODE"
        echo "$BODY" | jq .
    fi
else
    echo "⚠️  Skipped - No valid ID available"
fi
echo ""

# Test 8: Update Not Found
echo "====== Test 8: Update Not Found ======"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "http://localhost:8085/motelApi/v1/motelChains/00000000-0000-0000-0000-000000000000" \
  -H "Content-Type: application/json" \
  -d '{"motelChainName": "Test", "displayName": "Test", "state": "CA", "pincode": "90001", "status": "ACTIVE"}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "404" ]; then
    echo "✅ Status: $HTTP_CODE Not Found"
else
    echo "❌ Expected 404, got: $HTTP_CODE"
fi
echo "$BODY" | jq .
echo ""

# Test 9: Delete MotelChain
echo "====== Test 9: Delete MotelChain ======"
if [ -n "$TEST_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "http://localhost:8085/motelApi/v1/motelChains/$TEST_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "204" ]; then
        echo "✅ Status: $HTTP_CODE No Content"
        echo "$BODY" | jq .
    else
        echo "❌ Expected 204, got: $HTTP_CODE"
        echo "$BODY" | jq .
    fi
else
    echo "⚠️  Skipped - No valid ID available"
fi
echo ""

# Test 10: Delete Not Found
echo "====== Test 10: Delete Not Found ======"
RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "http://localhost:8085/motelApi/v1/motelChains/00000000-0000-0000-0000-000000000000")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "404" ]; then
    echo "✅ Status: $HTTP_CODE Not Found"
else
    echo "❌ Expected 404, got: $HTTP_CODE"
fi
echo "$BODY" | jq .
echo ""

echo "=========================================="
echo "  ✅ Test Suite Complete"
echo "=========================================="
