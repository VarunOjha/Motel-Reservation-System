#!/usr/bin/env bash
set -euo pipefail

# --------- Edit these defaults if you like ---------
export AWS_REGION="${AWS_REGION:-us-west-2}"
export CLUSTER_NAME="${CLUSTER_NAME:-motel-cluster-dev}"
export NAMESPACE="${NAMESPACE:-motel-cluster}"
# Bucket must be globally unique; suffix a timestamp to avoid collisions.
export BUCKET_NAME="${BUCKET_NAME:-motel-dev-loki-logs-20250823235420}"

# Helm release names
export LOKI_RELEASE="${LOKI_RELEASE:-loki}"
export PROMTAIL_RELEASE="${PROMTAIL_RELEASE:-promtail}"
export KPS_RELEASE="${KPS_RELEASE:-kps}"  # your kube-prometheus-stack release name


# kube-prometheus-stack (Grafana) release + namespace
: "${KPS_RELEASE:=kps}"
: "${KPS_NAMESPACE:=monitoring}"

# Loki release + its namespace (where your Loki service runs)
: "${LOKI_RELEASE:=loki}"
: "${NAMESPACE:=motel-cluster}"   # ← Loki's namespace (kept as-is for the datasource URL template)

mkdir -p generated
# Your template should build the URL from $LOKI_RELEASE and $NAMESPACE
# e.g., http://${LOKI_RELEASE}-gateway.${NAMESPACE}.svc.cluster.local (or :3100 to hit loki directly)
envsubst < grafana-loki-datasource.tmpl.yaml > generated/grafana-loki-datasource.yaml

echo "Installing/Upgrading kube-prometheus-stack ($KPS_RELEASE) in namespace $KPS_NAMESPACE with Loki datasource override ..."
helm upgrade --install "$KPS_RELEASE" prometheus-community/kube-prometheus-stack \
  --namespace "$KPS_NAMESPACE" \
  -f generated/grafana-loki-datasource.yaml \
  --reuse-values

echo "✅ Loki datasource provisioned in Grafana (release: $KPS_RELEASE, ns: $KPS_NAMESPACE). Check: Connections → Data sources"
