#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./delete-cluster.sh [CONFIG_FILE] [NAMESPACE] [KUBECONFIG_PATH] [--no-preclean] [-y]
#
# Defaults align with your create script & kubeconfig naming.
CONFIG_FILE="${1:-Cluster-config.yaml}"
NAMESPACE="${2:-motel-cluster}"
KUBECONFIG_PATH="${3:-./kubeconfig-motel-$(date +%Y%m%d)}"

# Flags
PRE_CLEAN=true
AUTO_YES=false
for arg in "${@:4}"; do
  case "$arg" in
    --no-preclean) PRE_CLEAN=false ;;
    -y|--yes) AUTO_YES=true ;;
  esac
done

have() { command -v "$1" >/dev/null 2>&1; }
die() { echo "ERROR: $*" >&2; exit 1; }

# --- Read cluster name & region from eksctl config (yq if present, awk fallback) ---
CLUSTER_NAME=""
REGION=""

if have yq; then
  CLUSTER_NAME="$(yq '.metadata.name' "$CONFIG_FILE" 2>/dev/null || true)"
  REGION="$(yq '.metadata.region' "$CONFIG_FILE" 2>/dev/null || true)"
else
  # Minimal fallback parser
  CLUSTER_NAME="$(awk '
    $1=="metadata:" {inmeta=1; next}
    inmeta && $1=="name:" {print $2; exit}
  ' "$CONFIG_FILE" 2>/dev/null || true)"
  REGION="$(awk '
    $1=="metadata:" {inmeta=1; next}
    inmeta && $1=="region:" {print $2; exit}
  ' "$CONFIG_FILE" 2>/dev/null || true)"
fi

[[ -n "$CLUSTER_NAME" && "$CLUSTER_NAME" != "null" ]] || die "Could not determine cluster name from $CONFIG_FILE"
[[ -n "$REGION" && "$REGION" != "null" ]] || die "Could not determine region from $CONFIG_FILE"

echo "About to DELETE EKS cluster:"
echo "  Cluster : $CLUSTER_NAME"
echo "  Region  : $REGION"
echo "  Config  : $CONFIG_FILE"
echo "  KubeCfg : $KUBECONFIG_PATH (if present)"
echo "  NS      : $NAMESPACE"
echo "  Preclean: $PRE_CLEAN"
echo

# --- y/n confirmation (no need to type name) ---
if ! $AUTO_YES; then
  read -r -p "Proceed with deletion of cluster '${CLUSTER_NAME}' in region '${REGION}'? [y/N]: " CONFIRM
  case "${CONFIRM:-}" in
    y|Y|yes|YES) ;;
    *) die "Aborted by user."; ;;
  esac
fi

# --- If kubeconfig exists, prefer it for kubectl pre-clean steps ---
if [[ -f "$KUBECONFIG_PATH" ]]; then
  export KUBECONFIG="$KUBECONFIG_PATH"
  echo "Using KUBECONFIG=$KUBECONFIG_PATH"
else
  echo "Kubeconfig '$KUBECONFIG_PATH' not found; will proceed without kubectl pre-clean if unreachable."
fi

# Try to find a context (best effort)
CLUSTER_CTX=""
if have kubectl; then
  set +e
  CLUSTER_CTX="$(kubectl config get-contexts -o name 2>/dev/null | grep "/${CLUSTER_NAME}$" | head -n1)"
  [[ -z "$CLUSTER_CTX" ]] && CLUSTER_CTX="$(kubectl config get-contexts -o name 2>/dev/null | head -n1)"
  set -e
fi

# --- Pre-clean (Ingresses + LB Services + app namespace), portable (no mapfile) ---
if $PRE_CLEAN && have kubectl; then
  echo "Pre-cleaning cluster resources (best-effort)..."

  if kubectl --context "$CLUSTER_CTX" get ns >/dev/null 2>&1; then
    # Delete all Ingresses (helps ALB Controller tear down)
    echo "• Deleting all Ingresses (-A)..."
    kubectl --context "$CLUSTER_CTX" delete ingress --all -A --ignore-not-found || true

    # Delete Services of type LoadBalancer across all namespaces
    echo "• Deleting all Services of type LoadBalancer (-A)..."
    kubectl --context "$CLUSTER_CTX" get svc -A \
      -o jsonpath='{range .items[?(@.spec.type=="LoadBalancer")]}{.metadata.namespace}{"\t"}{.metadata.name}{"\n"}{end}' \
      | while IFS=$'\t' read -r ns name; do
          if [[ -n "${ns:-}" && -n "${name:-}" ]]; then
            kubectl --context "$CLUSTER_CTX" -n "$ns" delete svc "$name" --ignore-not-found || true
          fi
        done

    # Optionally clear your application namespace to speed up node drains
    echo "• Deleting namespaced resources in '$NAMESPACE' (best-effort)..."
    kubectl --context "$CLUSTER_CTX" -n "$NAMESPACE" delete all --all --ignore-not-found || true
    kubectl --context "$CLUSTER_CTX" -n "$NAMESPACE" delete pvc --all --ignore-not-found || true
  else
    echo "kubectl cannot reach the cluster; skipping pre-clean."
  fi
else
  echo "Skipping pre-clean."
fi

# --- Delete with eksctl ---
have eksctl || die "eksctl is required to delete the cluster."

echo
echo "Deleting cluster via eksctl..."
eksctl delete cluster -f "$CONFIG_FILE" --wait
echo "eksctl delete initiated."

echo
echo "Local cleanup tips (optional):"
echo "• Remove kubeconfig entries for $CLUSTER_NAME (safe if no longer needed):"
echo "    kubectl config get-contexts -o name | grep '/$CLUSTER_NAME\$' | xargs -I{} kubectl config delete-context '{}' || true"
echo "    kubectl config get-clusters | grep \"$CLUSTER_NAME\" | xargs -I{} kubectl config delete-cluster '{}' || true"
echo "• Remove local kubeconfig file if it was only for this cluster:"
echo "    rm -f \"$KUBECONFIG_PATH\"  # optional"
echo
echo "Done."
