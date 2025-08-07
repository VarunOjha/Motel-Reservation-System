#!/bin/bash
set -e

echo "Building Docker image..."
docker build -t motel-api-gateway .

echo "Logging in to Amazon ECR..."
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 520320208231.dkr.ecr.us-west-2.amazonaws.com/motel-api-gateway

echo "Pushing Docker image to Amazon ECR..."
docker push 520320208231.dkr.ecr.us-west-2.amazonaws.com/motel-api-gateway:latest


echo "Creating Docker buildx builder..."
docker buildx build --platform linux/amd64 -t 520320208231.dkr.ecr.us-west-2.amazonaws.com/motel-api-gateway:latest --push .
