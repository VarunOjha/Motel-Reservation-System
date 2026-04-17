#!/bin/bash
set -e

echo "Building Docker image..."
docker build -t reservation-apis .

echo "Logging in to Amazon ECR..."
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com/reservation-apis

echo "Pushing Docker image to Amazon ECR..."
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com/reservation-apis:latest

echo "Creating Docker buildx builder..."
docker buildx build --platform linux/amd64 -t ${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com/reservation-apis:latest --push .
