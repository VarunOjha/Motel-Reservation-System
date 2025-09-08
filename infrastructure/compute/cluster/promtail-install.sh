#!/usr/bin/env bash
set -euo pipefail

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

: "${NAMESPACE:?NAMESPACE not set}"
: "${PROMTAIL_RELEASE:?PROMTAIL_RELEASE not set}"
: "${LOKI_RELEASE:?LOKI_RELEASE not set}"
: "${CLUSTER_NAME:?CLUSTER_NAME not set}"

mkdir -p generated
envsubst < promtail-values.tmpl.yaml > generated/promtail-values.yaml

echo "Installing/Upgrading grafana/promtail as $PROMTAIL_RELEASE in $NAMESPACE ..."
helm upgrade --install "$PROMTAIL_RELEASE" grafana/promtail \
  --namespace "$NAMESPACE" \
  -f generated/promtail-values.yaml

echo "Waiting for DaemonSet to be ready ..."
kubectl -n "$NAMESPACE" rollout status daemonset/"$PROMTAIL_RELEASE" --timeout=2m
kubectl -n "$NAMESPACE" get pods -l app.kubernetes.io/name=promtail -o wide
