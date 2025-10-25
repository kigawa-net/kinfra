# Kubernetes main configuration

# Create namespace for monitoring
resource "kubernetes_namespace" "monitoring" {
  metadata {
    name = "monitoring"
    labels = {
      name = "monitoring"
    }
  }
}

# Example: ConfigMap for monitoring configuration
resource "kubernetes_config_map" "monitoring-config" {
  metadata {
    name      = "monitoring-config"
    namespace = kubernetes_namespace.monitoring.metadata[0].name
  }

  data = {
    "prometheus.yml" = <<-EOT
      global:
        scrape_interval: 15s
        evaluation_interval: 15s
      
      rule_files:
        # - "first_rules.yml"
        # - "second_rules.yml"
      
      scrape_configs:
        - job_name: 'prometheus'
          static_configs:
            - targets: ['localhost:9090']
    EOT
  }
}

# Output namespace name
output "monitoring_namespace" {
  value = kubernetes_namespace.monitoring.metadata[0].name
}