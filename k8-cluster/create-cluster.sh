#!/bin/bash

CONFIG_FILE="cluster-config.yaml"

echo "Creating EKS cluster from config: $CONFIG_FILE..."
eksctl create cluster -f "$CONFIG_FILE"
echo "Cluster creation complete."
