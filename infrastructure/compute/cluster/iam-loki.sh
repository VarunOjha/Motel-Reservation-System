#!/usr/bin/env bash
set -euo pipefail

export BUCKET_NAME=motel-dev-loki-logs-20250823235420

# ---- Defaults (override via env) ----
CLUSTER_NAME="${CLUSTER_NAME:-motel-cluster-dev}"
NAMESPACE="${NAMESPACE:-motel-cluster}"
AWS_REGION="${AWS_REGION:-us-west-2}"

# ---- Required: S3 bucket name for Loki data ----
if [ -z "${BUCKET_NAME:-}" ]; then
  echo "ERROR: BUCKET_NAME not set. Example:"
  echo "  export BUCKET_NAME=motel-dev-loki-logs-20250824"
  exit 1
fi

# ---- Discover AWS account id (or honor provided) ----
AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-$(aws sts get-caller-identity --query Account --output text 2>/dev/null || true)}"
if [ -z "$AWS_ACCOUNT_ID" ] || [ "$AWS_ACCOUNT_ID" = "None" ]; then
  echo "ERROR: Could not determine AWS_ACCOUNT_ID (aws sts failed)."
  echo "Set it explicitly: export AWS_ACCOUNT_ID=123456789012"
  exit 1
fi

# ---- Discover EKS OIDC provider (or honor provided) ----
OIDC_PROVIDER="${OIDC_PROVIDER:-$(aws eks describe-cluster --name "$CLUSTER_NAME" --query "cluster.identity.oidc.issuer" --output text 2>/dev/null | sed 's~https://~~')}"
if [ -z "$OIDC_PROVIDER" ] || [ "$OIDC_PROVIDER" = "None" ]; then
  echo "ERROR: Could not fetch OIDC_PROVIDER for cluster $CLUSTER_NAME."
  echo "Make sure IRSA is associated and your proxy/CA are set (see earlier NO_PROXY/CA tips), then try again."
  exit 1
fi

echo "Using:"
echo "  CLUSTER_NAME   = $CLUSTER_NAME"
echo "  NAMESPACE      = $NAMESPACE"
echo "  AWS_REGION     = $AWS_REGION"
echo "  AWS_ACCOUNT_ID = $AWS_ACCOUNT_ID"
echo "  OIDC_PROVIDER  = $OIDC_PROVIDER"
echo "  BUCKET_NAME    = $BUCKET_NAME"

POLICY_NAME="LokiS3Access-${CLUSTER_NAME}"
ROLE_NAME="LokiIRSA-${CLUSTER_NAME}"

# ---- Create/ensure IAM policy ----
policy_arn="$(aws iam list-policies --query "Policies[?PolicyName=='${POLICY_NAME}'].Arn" --output text || true)"
if [ -z "$policy_arn" ] || [ "$policy_arn" = "None" ]; then
  echo "Creating IAM policy $POLICY_NAME ..."
  cat > /tmp/loki-s3-policy.json <<JSON
{
  "Version": "2012-10-17",
  "Statement": [
    { "Sid": "AllowList", "Effect": "Allow",
      "Action": ["s3:ListBucket","s3:GetBucketLocation"],
      "Resource": ["arn:aws:s3:::${BUCKET_NAME}"] },
    { "Sid": "AllowObjectOps", "Effect": "Allow",
      "Action": ["s3:PutObject","s3:GetObject","s3:DeleteObject","s3:ListBucketMultipartUploads","s3:AbortMultipartUpload"],
      "Resource": ["arn:aws:s3:::${BUCKET_NAME}/*"] }
  ]
}
JSON
  aws iam create-policy --policy-name "${POLICY_NAME}" --policy-document file:///tmp/loki-s3-policy.json >/dev/null
  policy_arn="$(aws iam list-policies --query "Policies[?PolicyName=='${POLICY_NAME}'].Arn" --output text)"
else
  echo "IAM policy exists: $policy_arn"
fi

# ---- Create/ensure IRSA role for SA loki in $NAMESPACE ----
role_arn="$(aws iam get-role --role-name "${ROLE_NAME}" --query "Role.Arn" --output text 2>/dev/null || true)"
if [ -z "$role_arn" ] || [ "$role_arn" = "None" ]; then
  echo "Creating IAM role $ROLE_NAME for IRSA ..."
  cat > /tmp/trust-policy.json <<JSON
{
  "Version": "2012-10-17",
  "Statement": [
    { "Effect": "Allow",
      "Principal": { "Federated": "arn:aws:iam::${AWS_ACCOUNT_ID}:oidc-provider/${OIDC_PROVIDER}" },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "${OIDC_PROVIDER}:sub": "system:serviceaccount:${NAMESPACE}:loki",
          "${OIDC_PROVIDER}:aud": "sts.amazonaws.com"
        }
      }
    }
  ]
}
JSON
  aws iam create-role --role-name "${ROLE_NAME}" --assume-role-policy-document file:///tmp/trust-policy.json >/dev/null
  aws iam attach-role-policy --role-name "${ROLE_NAME}" --policy-arn "${policy_arn}"
  role_arn="$(aws iam get-role --role-name "${ROLE_NAME}" --query "Role.Arn" --output text)"
  echo "Created role: $role_arn"
else
  echo "IAM role exists: $role_arn"
fi

# ---- Persist for Helm values rendering (if you use my install script) ----
mkdir -p generated
printf "export LOKI_ROLE_ARN='%s'\n" "$role_arn" > generated/env-loki-role.sh
echo "Wrote generated/env-loki-role.sh"
echo "✅ IAM ready."
