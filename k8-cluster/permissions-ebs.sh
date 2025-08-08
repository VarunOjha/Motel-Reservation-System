#!/usr/bin/env bash
set -euo pipefail

# ---------- INPUT ----------
YAML_FILE="${1:-cluster-config.yaml}"
AWS_PROFILE="${AWS_PROFILE:-}"   # optional

# Helper that safely injects --profile if provided
awsx() {
  if [[ -n "$AWS_PROFILE" ]]; then
    aws --profile "$AWS_PROFILE" "$@"
  else
    aws "$@"
  fi
}

# ---------- PARSE YAML (no yq) ----------
if [[ ! -f "$YAML_FILE" ]]; then
  echo "ERROR: File '$YAML_FILE' not found." >&2
  exit 1
fi

CLUSTER_NAME="$(awk '
  $1=="metadata:" {inmeta=1; next}
  inmeta && $1=="name:"   {print $2; exit}
' "$YAML_FILE")"

CLUSTER_REGION="$(awk '
  $1=="metadata:" {inmeta=1; next}
  inmeta && $1=="region:" {print $2; exit}
' "$YAML_FILE")"

if [[ -z "${CLUSTER_NAME:-}" || -z "${CLUSTER_REGION:-}" ]]; then
  echo "ERROR: Could not parse cluster name/region from $YAML_FILE" >&2
  exit 1
fi

echo "Cluster name: $CLUSTER_NAME"
echo "Region: $CLUSTER_REGION"

# ---------- PARTITION DETECTION ----------
CALLER_ARN="$(awsx sts get-caller-identity --query Arn --output text)"
PARTITION="$(printf "%s" "$CALLER_ARN" | awk -F: '{print $2}')"
[[ -z "$PARTITION" || "$PARTITION" == "None" ]] && PARTITION="aws"
POLICY_PREFIX="arn:${PARTITION}:iam::aws:policy"

# Policies for the eksctl ServiceRole
read -r -d '' SERVICEROLE_POLICY_ARNS <<EOF || true
${POLICY_PREFIX}/service-role/AmazonEBSCSIDriverPolicy
${POLICY_PREFIX}/AmazonEC2ContainerRegistryReadOnly
${POLICY_PREFIX}/AmazonEKSClusterPolicy
${POLICY_PREFIX}/AmazonEKSVPCResourceController
EOF

# Policy for NodeInstanceRole(s)
read -r -d '' NODEROLE_POLICY_ARNS <<EOF || true
${POLICY_PREFIX}/service-role/AmazonEBSCSIDriverPolicy
EOF

# ---------- FIND CLUSTER-RELATED ROLES ----------
SEARCH_REGEX="${CLUSTER_NAME}"
SERVICE_ROLE_FILTER='^eksctl-.*ServiceRole'
NODE_ROLE_FILTER='NodeInstanceRole'

echo "Discovering IAM roles matching /$SEARCH_REGEX/ ..."
ALL_ROLE_NAMES="$(
  awsx iam list-roles --query 'Roles[].RoleName' --output text \
  | tr '\t' '\n' \
  | grep -E "$SEARCH_REGEX" || true
)"

if [[ -z "$ALL_ROLE_NAMES" ]]; then
  echo "ERROR: No IAM roles matched regex '$SEARCH_REGEX'." >&2
  exit 1
fi

IAM_ROLES="$(echo "$ALL_ROLE_NAMES" | tr '\n' ' ' | sed 's/[[:space:]]\{1,\}/ /g')"
echo "IAM roles for cluster '${CLUSTER_NAME}':"
echo "$IAM_ROLES"

# ---------- ATTACH TO ServiceRole ----------
SERVICE_ROLE_NAME="$(echo "$ALL_ROLE_NAMES" | grep -E "$SERVICE_ROLE_FILTER" | sort | head -n 1 || true)"
if [[ -n "$SERVICE_ROLE_NAME" ]]; then
  echo "Using ServiceRole: $SERVICE_ROLE_NAME (partition: $PARTITION)"
  awsx iam get-role --role-name "$SERVICE_ROLE_NAME" >/dev/null

  echo "Attaching policies to ServiceRole: $SERVICE_ROLE_NAME"
  while IFS= read -r ARN; do
    [[ -z "$ARN" ]] && continue
    ATTACHED="$(awsx iam list-attached-role-policies \
      --role-name "$SERVICE_ROLE_NAME" \
      --query "AttachedPolicies[?PolicyArn=='${ARN}'] | length(@)" \
      --output text || echo 0)"
    if [[ "$ATTACHED" == "0" ]]; then
      echo "  -> Attaching $ARN"
      awsx iam attach-role-policy --role-name "$SERVICE_ROLE_NAME" --policy-arn "$ARN"
    else
      echo "  -> Already attached: $ARN"
    fi
  done <<< "$SERVICEROLE_POLICY_ARNS"
else
  echo "WARN: No ServiceRole matched filter '$SERVICE_ROLE_FILTER'. Skipping ServiceRole attachments."
fi

# ---------- ATTACH TO NodeInstanceRole(s) ----------
NODE_ROLE_NAMES="$(echo "$ALL_ROLE_NAMES" | grep -E "$NODE_ROLE_FILTER" || true)"
if [[ -z "$NODE_ROLE_NAMES" ]]; then
  echo "WARN: No NodeInstanceRole matched filter '$NODE_ROLE_FILTER'. Skipping node policy attachments."
else
  echo "NodeInstanceRole(s) detected:"
  echo "$NODE_ROLE_NAMES"
  while IFS= read -r NODE_ROLE; do
    [[ -z "$NODE_ROLE" ]] && continue
    echo "Attaching node policies to: $NODE_ROLE"
    while IFS= read -r ARN; do
      [[ -z "$ARN" ]] && continue
      ATTACHED="$(awsx iam list-attached-role-policies \
        --role-name "$NODE_ROLE" \
        --query "AttachedPolicies[?PolicyArn=='${ARN}'] | length(@)" \
        --output text || echo 0)"
      if [[ "$ATTACHED" == "0" ]]; then
        echo "  -> Attaching $ARN"
        awsx iam attach-role-policy --role-name "$NODE_ROLE" --policy-arn "$ARN"
      else
        echo "  -> Already attached: $ARN"
      fi
    done <<< "$NODEROLE_POLICY_ARNS"
  done <<< "$NODE_ROLE_NAMES"
fi

# ---------- EBS CSI ADDON (create or update) ----------
echo "Ensuring aws-ebs-csi-driver addon on cluster '${CLUSTER_NAME}' in ${CLUSTER_REGION}..."
if awsx eks describe-addon --cluster-name "$CLUSTER_NAME" --addon-name aws-ebs-csi-driver --region "$CLUSTER_REGION" >/dev/null 2>&1; then
  echo "Addon exists; updating with OVERWRITE..."
  awsx eks update-addon --cluster-name "$CLUSTER_NAME" --addon-name aws-ebs-csi-driver --region "$CLUSTER_REGION" --resolve-conflicts OVERWRITE
else
  echo "Creating addon..."
  awsx eks create-addon --cluster-name "$CLUSTER_NAME" --addon-name aws-ebs-csi-driver --region "$CLUSTER_REGION" --resolve-conflicts OVERWRITE
fi

echo "Done."
