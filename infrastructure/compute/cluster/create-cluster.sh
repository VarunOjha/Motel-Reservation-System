#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./create-cluster.sh [CONFIG_FILE] [NAMESPACE] [KUBECONFIG_OUT]
# Defaults mirror your original filenames.
CONFIG_FILE="${1:-Cluster-config.yaml}"
NAMESPACE="${2:-motel-cluster}"
# If you pass a kubeconfig path, we'll use it; otherwise timestamped file.
KUBECONFIG_OUT="${3:-./kubeconfig-motel-$(date +%Y%m%d)}"

echo "Creating EKS cluster from config: $CONFIG_FILE ..."
eksctl create cluster -f "$CONFIG_FILE" --kubeconfig "$KUBECONFIG_OUT"
echo "Cluster creation complete."

# --- Derive cluster name from the config (prefer yq if available) ---
if command -v yq >/dev/null 2>&1; then
  CLUSTER_NAME="$(yq '.metadata.name' "$CONFIG_FILE" 2>/dev/null || true)"
else
  # very small/robust fallback: extract the value under 'metadata.name'
  CLUSTER_NAME="$(awk '
    $1=="metadata:" {inmeta=1}
    inmeta && $1=="name:" {print $2; exit}
  ' "$CONFIG_FILE" 2>/dev/null || true)"
fi

# Pick context from the new kubeconfig
export KUBECONFIG="$KUBECONFIG_OUT"
if [[ -n "${CLUSTER_NAME:-}" && "${CLUSTER_NAME}" != "null" ]]; then
  # EKS context typically ends with "/<cluster-name>"
  CLUSTER_CTX="$(kubectl config get-contexts -o name | grep "/${CLUSTER_NAME}$" || true)"
fi
# Fallback to the first context if we didn't match by name
CLUSTER_CTX="${CLUSTER_CTX:-$(kubectl config get-contexts -o name | head -n1)}"

echo "Using context: ${CLUSTER_CTX}"

# --- Ensure namespace exists (no LimitRange involved) ---
if ! kubectl --context "$CLUSTER_CTX" get ns "$NAMESPACE" >/dev/null 2>&1; then
  if [[ -f "motel-namespace.yaml" ]]; then
    echo "Creating namespace via YAML: $NAMESPACE"
    kubectl --context "$CLUSTER_CTX" apply -f motel-namespace.yaml
  else
    echo "Creating namespace directly: $NAMESPACE"
    kubectl --context "$CLUSTER_CTX" create namespace "$NAMESPACE"
  fi
fi

# Make this namespace the default for this context
kubectl --context "$CLUSTER_CTX" config set-context "$CLUSTER_CTX" --namespace="$NAMESPACE" >/dev/null

cat <<EOF

Done ✅
Kubeconfig: ${KUBECONFIG_OUT}
Context:    ${CLUSTER_CTX}
Namespace:  ${NAMESPACE} (set as default)
EOF
