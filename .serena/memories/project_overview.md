# kinfra Project Overview

## Purpose
Kotlin-based Terraform wrapper that simplifies infrastructure management with Bitwarden Secret Manager integration for secure secret handling.

## Tech Stack
- **Language**: Kotlin 2.2.0
- **Runtime**: JDK 21
- **Build Tool**: Gradle 8.10.2
- **Infrastructure**: Terraform
- **Secrets**: Bitwarden Secret Manager
- **Storage**: Cloudflare R2 (S3-compatible)
- **Web Framework**: Ktor
- **Serialization**: kotlinx.serialization, Gson, kaml
- **DI**: Koin
- **Testing**: JUnit 5

## Architecture
Multi-module Gradle project:
- **kinfra-api**: Core interfaces and models
- **action**: Business logic interfaces
- **kinfra-infra**: Infrastructure implementations (R2, Bitwarden, etc.)
- **app-cli**: Command-line interface
- **app-web**: Web API server
- **kodel**: Code generation utilities

## Key Features
- Terraform operation simplification
- Secure secret management via Bitwarden
- Dual interface: CLI and Web API
- Modular architecture for maintainability
- Support for various deployment types (Kubernetes, SSH, etc.)

## Interfaces
- **CLI**: Commands like `kinfra init`, `plan`, `apply`, `deploy`
- **Web API**: REST endpoints for automation

## Configuration
- Environment variables for secrets and settings
- YAML-based project configuration
- Support for sub-projects

## Development Environment
- Linux-based development
- Gradle wrapper for consistent builds
- Serena for advanced code analysis (Claude Code integration)