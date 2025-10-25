terraform {
  required_version = ">= 1.5.0"
  
  backend "s3" {
    # These values will be populated by kinfra from Bitwarden
    # bucket = ""
    # key = ""
    # region = ""
    # endpoint = ""
    # access_key = ""
    # secret_key = ""
    # skip_credentials_validation = true
    # skip_metadata_api_check = true
  }
  
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.20"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.9"
    }
  }
}

provider "kubernetes" {
  config_path = var.kubernetes_config_path
  config_context = var.kubernetes_config_context
}

provider "helm" {
  kubernetes {
    config_path = var.kubernetes_config_path
    config_context = var.kubernetes_config_context
  }
}

variable "kubernetes_config_path" {
  description = "Path to kubeconfig file"
  type        = string
  default     = "~/.kube/config"
}

variable "kubernetes_config_context" {
  description = "Kubernetes config context to use"
  type        = string
  default     = ""
}