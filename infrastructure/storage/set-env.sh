#!/usr/bin/env bash
# shellcheck disable=SC2086
set -euo pipefail

# --------- Edit these defaults if you like ---------
export AWS_REGION="${AWS_REGION:-us-west-2}"
export CLUSTER_NAME="${CLUSTER_NAME:-motel-cluster-dev}"
export NAMESPACE="${NAMESPACE:-motel-cluster}"
# Bucket must be globally unique; suffix a timestamp to avoid collisions.
export BUCKET_NAME="${BUCKET_NAME:-motel-dev-loki-logs-$(date +%Y%m%d%H%M%S)}"

# Helm release names
export LOKI_RELEASE="${LOKI_RELEASE:-loki}"
export PROMTAIL_RELEASE="${PROMTAIL_RELEASE:-promtail}"
export KPS_RELEASE="${KPS_RELEASE:-kps}"  # your kube-prometheus-stack release name

# StorageClass for Loki's small local cache PVC
export STORAGE_CLASS="${STORAGE_CLASS:-gp3}"

# ---------------------------------------------------
# Tool checks
need() { command -v "$1" >/dev/null 2>&1 || { echo "Missing dependency: $1"; exit 1; }; }
need aws
need kubectl
need helm
need eksctl

# AWS account id
export AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-$(aws sts get-caller-identity --query Account --output text)}"

# EKS OIDC issuer (without https://)
export OIDC_PROVIDER="${OIDC_PROVIDER:-$(aws eks describe-cluster --name "$CLUSTER_NAME" --query "cluster.identity.oidc.issuer" --output text | sed 's~https://~~')}"

# Prepare namespace
kubectl get ns "$NAMESPACE" >/dev/null 2>&1 || kubectl create ns "$NAMESPACE"

# Helm repos
helm repo add grafana https://grafana.github.io/helm-charts >/dev/null 2>&1 || true
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts >/dev/null 2>&1 || true
helm repo update >/dev/null 2>&1 || true

cat <<EOF
Environment:
  AWS_REGION     = $AWS_REGION
  AWS_ACCOUNT_ID = $AWS_ACCOUNT_ID
  CLUSTER_NAME   = $CLUSTER_NAME
  NAMESPACE      = $NAMESPACE
  BUCKET_NAME    = $BUCKET_NAME
  LOKI_RELEASE   = $LOKI_RELEASE
  PROMTAIL_RELEASE = $PROMTAIL_RELEASE
  KPS_RELEASE    = $KPS_RELEASE
  STORAGE_CLASS  = $STORAGE_CLASS
  OIDC_PROVIDER  = $OIDC_PROVIDER
EOF
