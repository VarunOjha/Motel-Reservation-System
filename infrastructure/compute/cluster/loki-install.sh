#!/usr/bin/env bash
set -euo pipefail


export AWS_REGION="${AWS_REGION:-us-west-2}"
export CLUSTER_NAME="${CLUSTER_NAME:-motel-cluster-dev}"
export NAMESPACE="${NAMESPACE:-motel-cluster}"
# Bucket must be globally unique; suffix a timestamp to avoid collisions.
export BUCKET_NAME="${BUCKET_NAME:-motel-dev-loki-logs-20250823235420}"

# Helm release names
export LOKI_RELEASE="${LOKI_RELEASE:-loki}"
export PROMTAIL_RELEASE="${PROMTAIL_RELEASE:-promtail}"
export KPS_RELEASE="${KPS_RELEASE:-kps}"  # your kube-prometheus-stack release name

# StorageClass for Loki's small local cache PVC
export STORAGE_CLASS="${STORAGE_CLASS:-gp3}"

: "${AWS_REGION:?AWS_REGION not set}"
: "${BUCKET_NAME:?BUCKET_NAME not set}"
: "${NAMESPACE:?NAMESPACE not set}"
: "${LOKI_RELEASE:?LOKI_RELEASE not set}"
: "${STORAGE_CLASS:?STORAGE_CLASS not set}"

# Load role ARN exported by the previous step
if [ -f generated/env-loki-role.sh ]; then
  # shellcheck disable=SC1091
  source generated/env-loki-role.sh
fi
: "${LOKI_ROLE_ARN:?LOKI_ROLE_ARN not set, run 03-iam-loki.sh}"

# Render values from template
mkdir -p generated
envsubst < loki-values.tmpl.yaml > generated/loki-values.yaml

echo "Installing/Upgrading grafana/loki as $LOKI_RELEASE in $NAMESPACE ..."
helm upgrade --install "$LOKI_RELEASE" grafana/loki \
  --namespace "$NAMESPACE" \
  -f generated/loki-values.yaml

echo "Waiting for pods to come up ..."
kubectl -n "$NAMESPACE" get pods -l app.kubernetes.io/name=loki -o wide
