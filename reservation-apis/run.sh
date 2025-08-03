#!/bin/bash
set -e

# Navigate to your project directory (optional, but good practice)
# cd /path/to/your/docker/project

echo "Running docker-compose down -v ..."
docker-compose down -v

echo "Running docker-compose build --no-cache ..."
docker-compose build --no-cache


echo "Starting Docker w docker-compose up"
docker-compose up

echo "Well, this worked baby!"