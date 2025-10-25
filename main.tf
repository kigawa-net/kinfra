terraform {
  required_providers {
    null = {
      source  = "hashicorp/null"
      version = "~> 3.0"
    }
  }

  backend "s3" {
    # Backend config will be provided via -backend-config
  }
}

resource "null_resource" "test" {
  provisioner "local-exec" {
    command = "echo 'Hello from Terraform'"
  }
}