#!/bin/bash

# Cluster name
CLUSTER_NAME="motel-cluster-dev"

# Get IAM role names for the cluster
roles=$(aws iam list-roles \
  --query "Roles[?contains(RoleName, '${CLUSTER_NAME}')].RoleName" \
  --output text)

# Store roles in a bash variable
IAM_ROLES="$roles"

# Echo the variable
echo "IAM roles for cluster '${CLUSTER_NAME}':"
echo "$IAM_ROLES"
