#!/usr/bin/env bash

export AWS_REGION="${AWS_REGION:-us-west-2}"
export CLUSTER_NAME="${CLUSTER_NAME:-motel-cluster-dev}"
export NAMESPACE="${NAMESPACE:-motel-cluster}"

set -euo pipefail
: "${CLUSTER_NAME:?CLUSTER_NAME not set}"

echo "Associating IAM OIDC provider to cluster (idempotent) ..."
eksctl utils associate-iam-oidc-provider --cluster "$CLUSTER_NAME" --approve
echo "✅ IRSA OIDC provider ensured."
