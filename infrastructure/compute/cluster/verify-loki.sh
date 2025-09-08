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

: "${NAMESPACE:?NAMESPACE not set}"
: "${LOKI_RELEASE:?LOKI_RELEASE not set}"
: "${PROMTAIL_RELEASE:?PROMTAIL_RELEASE not set}"

echo "---- Loki pods ----"
kubectl -n "$NAMESPACE" get pods -l app.kubernetes.io/name=loki -o wide || true

echo "---- Promtail pods ----"
kubectl -n "$NAMESPACE" get pods -l app.kubernetes.io/name=promtail -o wide || true

echo "---- Services ----"
kubectl -n "$NAMESPACE" get svc | grep -E "$LOKI_RELEASE|promtail" || true

echo "---- Recent Promtail logs (1 pod) ----"
POD=$(kubectl -n "$NAMESPACE" get pods -l app.kubernetes.io/name=promtail -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)
if [ -n "$POD" ]; then
  kubectl -n "$NAMESPACE" logs "$POD" --tail=50 || true
fi

cat <<'TIP'

Next steps:
  1) In Grafana → Explore → pick "Loki", run a simple query:
       {namespace="motel-cluster"} |= "ERROR"
     or use the external label:
       {cluster="motel-cluster-dev"}

  2) If no logs appear yet, generate some pod logs or check Promtail logs for push errors.
TIP
