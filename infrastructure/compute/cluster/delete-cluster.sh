#!/bin/bash

YAML_FILE="${1:-cluster-config.yaml}"

if [[ ! -f "$YAML_FILE" ]]; then
  echo "ERROR: File '$YAML_FILE' not found."
  exit 1
fi

# Extract cluster name (first name: under metadata:)
CLUSTER_NAME="$(grep -E '^[[:space:]]*name:' "$YAML_FILE" \
  | head -n 1 \
  | awk '{print $2}')"

# Extract region (metadata.region)
REGION="$(grep -E '^[[:space:]]*region:' "$YAML_FILE" \
  | head -n 1 \
  | awk '{print $2}')"

if [[ -z "$CLUSTER_NAME" || -z "$REGION" ]]; then
  echo "ERROR: Cluster name or region not found in $YAML_FILE"
  exit 1
fi

echo "Cluster name: $CLUSTER_NAME"
echo "Region: $REGION"

echo "🗑️ Deleting EKS cluster: $CLUSTER_NAME in $REGION..."
eksctl delete cluster --name "$CLUSTER_NAME" --region "$REGION"
echo "✅ Cluster deletion initiated."
