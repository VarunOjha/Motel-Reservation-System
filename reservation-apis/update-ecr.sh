#!/bin/bash
set -e

echo "Building Docker image..."
docker build -t reservation-apis .

echo "Logging in to Amazon ECR..."
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 520320208231.dkr.ecr.us-west-2.amazonaws.com/reservation-apis

echo "Pushing Docker image to Amazon ECR..."
docker push 520320208231.dkr.ecr.us-west-2.amazonaws.com/reservation-apis:latest

echo "Creating Docker buildx builder..."
docker buildx build --platform linux/amd64 -t 520320208231.dkr.ecr.us-west-2.amazonaws.com/reservation-apis:latest --push .
