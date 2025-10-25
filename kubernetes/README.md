# Kubernetes Infrastructure

This directory contains Terraform configurations for Kubernetes resources.

## Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `kubernetes_config_path` | Path to kubeconfig file | `~/.kube/config` |
| `kubernetes_config_context` | Kubernetes config context to use | `""` |

## Backend Configuration

The S3 backend configuration is automatically populated by kinfra from Bitwarden secrets.

## Usage

```bash
# Initialize Terraform
kinfra sub init k8s

# Plan changes
kinfra sub plan k8s

# Apply changes
kinfra sub apply k8s
```