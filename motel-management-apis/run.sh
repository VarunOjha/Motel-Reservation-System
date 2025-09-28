#!/bin/bash
set -e

# Navigate to your project directory (optional, but good practice)
# cd /path/to/your/docker/project

echo "Running gradle build..."

./gradlew clean build

echo "Build completed successfully!"
echo "Waiting 2 seconds before running tests..."
sleep 2

echo "Running test suite..."
./gradlew test

echo "Tests completed successfully!"
echo "Waiting 2 seconds before building Docker image..."
sleep 2

echo "Starting Docker Compose with build..."
docker compose up --build

echo "Docker Compose command finished."