#!/usr/bin/env bash
set -euo pipefail

# ---- set your cluster + ns ----
CLUSTER_NAME="${CLUSTER_NAME:-motel-cluster-dev}"
NAMESPACE="${NAMESPACE:-motel-cluster}"
ROLE_NAME="${ROLE_NAME:-LokiIRSA-${CLUSTER_NAME}}"     # the IRSA role you created for Loki

# ---- discover AWS account and OIDC issuer ----
AWS_ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
ISSUER="$(aws eks describe-cluster --name "$CLUSTER_NAME" --query "cluster.identity.oidc.issuer" --output text | sed 's#https://##')"
OP_ARN="arn:aws:iam::${AWS_ACCOUNT_ID}:oidc-provider/${ISSUER}"

echo "Account: $AWS_ACCOUNT_ID"
echo "OIDC   : $ISSUER"
echo "OIDC ARN: $OP_ARN"

# ---- find the service account actually used by the Loki pod ----
POD="$(kubectl -n "$NAMESPACE" get pods -l app.kubernetes.io/name=loki -o jsonpath='{.items[0].metadata.name}')"
SA_NAME="$(kubectl -n "$NAMESPACE" get pod "$POD" -o jsonpath='{.spec.serviceAccountName}')"

echo "Loki pod   : $POD"
echo "SA in use  : $SA_NAME"

# ---- rebuild the role trust policy to exactly match this SA + issuer ----
cat > /tmp/loki-trust.json <<JSON
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Federated": "${OP_ARN}" },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "${ISSUER}:sub": "system:serviceaccount:${NAMESPACE}:${SA_NAME}",
          "${ISSUER}:aud": "sts.amazonaws.com"
        }
      }
    }
  ]
}
JSON

echo "Updating trust policy for role ${ROLE_NAME} ..."
aws iam update-assume-role-policy --role-name "$ROLE_NAME" --policy-document file:///tmp/loki-trust.json

# ---- annotate the ServiceAccount with the role ARN (idempotent) ----
ROLE_ARN="$(aws iam get-role --role-name "$ROLE_NAME" --query 'Role.Arn' --output text)"
kubectl -n "$NAMESPACE" annotate serviceaccount "$SA_NAME" \
  eks.amazonaws.com/role-arn="$ROLE_ARN" --overwrite

echo "ServiceAccount annotated with: $ROLE_ARN"

# ---- restart Loki to pick up the env + token ----
kubectl -n "$NAMESPACE" delete pod -l app.kubernetes.io/name=loki

# ---- sanity check once it comes up ----
echo "Waiting for new pod..."
kubectl -n "$NAMESPACE" wait --for=condition=Ready pod -l app.kubernetes.io/name=loki --timeout=5m

NEWPOD="$(kubectl -n "$NAMESPACE" get pods -l app.kubernetes.io/name=loki -o jsonpath='{.items[0].metadata.name}')"
echo "New pod: $NEWPOD"
echo "-- env expects to show AWS_ROLE_ARN + AWS_WEB_IDENTITY_TOKEN_FILE --"
kubectl -n "$NAMESPACE" exec "$NEWPOD" -- sh -c 'echo $AWS_ROLE_ARN; echo $AWS_WEB_IDENTITY_TOKEN_FILE; test -f "$AWS_WEB_IDENTITY_TOKEN_FILE" && echo "token: OK" || echo "token: MISSING"'

echo "✅ IRSA trust + SA annotation refreshed. Watch Loki logs for S3 success."
