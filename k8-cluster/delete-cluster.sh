#!/bin/bash

# ===== CONFIG =====
CLUSTER_NAME="motel-dev-cluster"  # Change as needed
REGION="us-west-2"                # Change to your AWS region

echo "🗑️ Deleting EKS cluster: $CLUSTER_NAME in $REGION..."
eksctl delete cluster --name "$CLUSTER_NAME" --region "$REGION"
echo "✅ Cluster deletion initiated."
