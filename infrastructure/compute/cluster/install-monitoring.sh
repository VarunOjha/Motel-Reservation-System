#!/usr/bin/env bash
set -euo pipefail

# -------- Config (edit if needed) --------
CLUSTER_NAME="${CLUSTER_NAME:-motel-cluster-dev}"
REGION="${CLUSTER_REGION:-us-west-2}"
NAMESPACE="${NAMESPACE:-monitoring}"
RELEASE_NAME="${RELEASE_NAME:-kps}"   # kube-prometheus-stack release name
# ----------------------------------------

need() { command -v "$1" >/dev/null 2>&1 || { echo "ERROR: $1 not found in PATH"; exit 1; }; }
need aws
need kubectl
need helm

echo ">> Using cluster: ${CLUSTER_NAME} (${REGION})"
aws eks update-kubeconfig --name "${CLUSTER_NAME}" --region "${REGION}" >/dev/null

# Create namespace if missing
kubectl get ns "${NAMESPACE}" >/dev/null 2>&1 || kubectl create namespace "${NAMESPACE}"

echo ">> Adding Helm repos"
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts >/dev/null
helm repo add grafana https://grafana.github.io/helm-charts >/dev/null
helm repo update >/dev/null

# Install kube-prometheus-stack
# - Expose Grafana with ELB (LoadBalancer)
# - Enable default dashboards and sidecar
# - Persist Grafana data to emptyDir by default; tweak persistence if desired
echo ">> Installing kube-prometheus-stack (this can take a few minutes)…"
helm upgrade --install "${RELEASE_NAME}" prometheus-community/kube-prometheus-stack \
  --namespace "${NAMESPACE}" \
  --set grafana.service.type=LoadBalancer \
  --set grafana.service.port=80 \
  --set grafana.adminPassword="$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 16)" \
  --set grafana.sidecar.dashboards.enabled=true \
  --set grafana.sidecar.datasources.enabled=true \
  --set grafana.defaultDashboardsEnabled=true \
  --set prometheus.service.type=ClusterIP \
  --set alertmanager.service.type=ClusterIP \
  --wait

echo ">> Waiting for pods to be Ready in namespace ${NAMESPACE}"
kubectl wait --namespace "${NAMESPACE}" --for=condition=Ready pods --all --timeout=10m

# Fetch Grafana info
GRAFANA_SVC="${RELEASE_NAME}-grafana"
echo ">> Fetching Grafana ELB address"
# Wait up to ~5 minutes for the ELB hostname to populate
for i in {1..30}; do
  GRAFANA_HOST=$(kubectl get svc "${GRAFANA_SVC}" -n "${NAMESPACE}" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)
  [[ -n "${GRAFANA_HOST:-}" ]] && break
  echo "   (waiting for ELB... ${i}/30)"
  sleep 10
done

# Get admin password (we set it explicitly, but also support reading from secret for good measure)
PASS_FROM_SECRET=$(kubectl get secret -n "${NAMESPACE}" "${RELEASE_NAME}-grafana" -o jsonpath='{.data.admin-password}' 2>/dev/null || true)
if [[ -n "${PASS_FROM_SECRET}" ]]; then
  GRAFANA_PASS="$(echo "$PASS_FROM_SECRET" | base64 --decode)"
else
  # Fallback if secret not readable; helm set above controls it, but this is rarely needed
  GRAFANA_PASS="admin"
fi

echo
echo "================== Monitoring Installed =================="
echo "Prometheus stack release : ${RELEASE_NAME}"
echo "Namespace                : ${NAMESPACE}"
if [[ -n "${GRAFANA_HOST:-}" ]]; then
  echo "Grafana URL              : http://${GRAFANA_HOST}"
  echo "Grafana admin user       : admin"
  echo "Grafana admin password   : ${GRAFANA_PASS}"
  echo "Note: ELB may take a few more minutes to be reachable."
else
  echo "Grafana Service          : ${GRAFANA_SVC}"
  echo "External hostname        : (pending)"
  echo "You can port-forward meanwhile:"
  echo "kubectl -n ${NAMESPACE} port-forward svc/${GRAFANA_SVC} 3000:80"
  echo "Then open: http://localhost:3000"
  echo "Grafana admin password   : ${GRAFANA_PASS}"
fi
echo "=========================================================="
echo

# Handy: how to uninstall later
cat <<EOF
To uninstall later:
  helm uninstall ${RELEASE_NAME} -n ${NAMESPACE}
  kubectl delete ns ${NAMESPACE}
EOF
