# triage-promtail.sh
#!/usr/bin/env bash
set -euo pipefail
NS=${NS:-motel-cluster}
REL=${REL:-promtail}

echo "== DaemonSet status =="
kubectl -n "$NS" describe ds "$REL" | sed -n '1,200p'

echo -e "\n== Pods =="
kubectl -n "$NS" get pods -l app.kubernetes.io/name=promtail -o wide

echo -e "\n== Pod reasons =="
kubectl -n "$NS" get pods -l app.kubernetes.io/name=promtail -o json \
  | jq -r '.items[] | [.metadata.name, .status.phase,
 (.status.containerStatuses[]?.state|keys[]? // "n/a")] | @tsv' || true

echo -e "\n== Recent namespace events (look for PodSecurity, ImagePull, Mount errors) =="
kubectl -n "$NS" get events --sort-by=.lastTimestamp | tail -n 60

echo -e "\n== Sample pod describe/logs =="
POD=$(kubectl -n "$NS" get pods -l app.kubernetes.io/name=promtail -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)
if [ -n "$POD" ]; then
  kubectl -n "$NS" describe pod "$POD" | sed -n '1,220p'
  echo -e "\n-- Last 200 log lines --"
  kubectl -n "$NS" logs "$POD" --tail=200 || true
fi

echo -e "\n== Node OS labels (check for Windows nodes) =="
kubectl get nodes -L kubernetes.io/os
