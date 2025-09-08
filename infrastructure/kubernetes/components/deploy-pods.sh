#!/usr/bin/env bash
set -euo pipefail

# Deploy all YAMLs from the current directory (recursively) into a single namespace.
# No kubeconfig path required—uses your current kubectl context.
#
# Usage:
#   ./deploy.sh [NAMESPACE]
#
# Notes:
# - If any manifest hard-codes metadata.namespace, that will override the -n flag.
# - Keep metadata.namespace unset in your manifests to rely on the script's namespace.

NAMESPACE="${1:-motel-cluster}"

echo "Using kubectl context:"
kubectl config current-context

echo "Ensuring namespace '${NAMESPACE}' exists..."
kubectl get ns "${NAMESPACE}" >/dev/null 2>&1 || kubectl create ns "${NAMESPACE}"

echo "Applying manifests from: $(pwd)"
# Apply everything under the current directory (recursively) into the namespace
kubectl -n "${NAMESPACE}" apply -R -f .

echo "Waiting for Deployments to roll out in namespace '${NAMESPACE}'..."
# Wait on all deployments discovered after apply
DEPLOYS="$(kubectl -n "${NAMESPACE}" get deploy -o name || true)"
if [[ -n "${DEPLOYS}" ]]; then
  for d in ${DEPLOYS}; do
    echo "→ Rollout status for ${d}"
    kubectl -n "${NAMESPACE}" rollout status "${d}" --timeout=5m
  done
else
  echo "No Deployments found."
fi

echo "Summary (namespace: ${NAMESPACE}):"
kubectl -n "${NAMESPACE}" get deploy,sts,ds,po,svc,ingress
