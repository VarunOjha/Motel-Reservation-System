#!/bin/sh
set -e

echo "Checking PostgreSQL availability at postgres:5432..."

until pg_isready -h postgres -p 5432 -U moteluser; do
  echo "Waiting for Postgres..."
  sleep 2
done

echo "Postgres is ready. Starting the Spring Boot application..."
exec java -jar app.jar
