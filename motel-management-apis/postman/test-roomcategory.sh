#!/bin/bash

# RoomCategory API Test Runner
# This script runs the Postman collection using Newman

echo "🧪 RoomCategory API - Test Suite Runner"
echo "========================================"
echo ""

# Check if newman is installed
if ! command -v newman &> /dev/null
then
    echo "❌ Newman is not installed!"
    echo "📦 Install it with: npm install -g newman"
    exit 1
fi

# Check if app is running
echo "🔍 Checking if application is running..."
if ! curl -s http://localhost:8085/actuator/health > /dev/null 2>&1; then
    echo "❌ Application is not running on port 8085"
    echo "🚀 Start it with: ./gradlew bootRun"
    exit 1
fi

echo "✅ Application is running"
echo ""

# Run the collection
echo "🏃 Running RoomCategory API tests..."
echo ""

newman run RoomCategory-API.postman_collection.json \
    --reporters cli,json \
    --reporter-json-export results-roomcategory.json \
    --color on \
    --delay-request 100

# Check exit code
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
    echo "📊 Results saved to: results-roomcategory.json"
else
    echo ""
    echo "❌ Some tests failed!"
    echo "📊 Check results-roomcategory.json for details"
    exit 1
fi
