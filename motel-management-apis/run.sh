#!/bin/bash
set -e

# Navigate to your project directory (optional, but good practice)
# cd /path/to/your/docker/project

echo "Running gradle build..."

./gradlew clean build

echo "Starting Docker Compose with build..."
docker-compose up --build

echo "Docker Compose command finished."