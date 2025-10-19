# Project Overview

## Purpose
Kotlin-based Terraform wrapper with Bitwarden Secret Manager integration for secure secret management.

## Tech Stack
- **Language**: Kotlin 2.2.0
- **Build Tool**: Gradle 8.10.2
- **JDK**: 21
- **Web Framework**: Ktor (for app-web)
- **DI**: Koin
- **Serialization**: Gson (JSON), kotlinx-serialization + kaml (YAML)
- **Testing**: JUnit
- **Linting/Formatting**: ktlint

## Interfaces
- **CLI** (`app-cli`): Command-line tool
- **Web API** (`app-web`): REST API on port 8080

## Architecture
Multi-module layered architecture:
- **model**: Domain models and interfaces (no dependencies)
- **action**: Business logic contracts
- **infrastructure**: Implementations (Terraform, Bitwarden, config, logging)
- **app-cli**: CLI application
- **app-web**: Web application

## Key Features
- Simplified Terraform operations
- Secure secret management via Bitwarden
- Modular design for maintainability
- ANSI colored output
- Environment variable configuration

## Configuration
- Global config: `~/.local/kinfra/project.json`
- Project config: `kinfra.yaml` (auto-generated on login)
- Backend config: `backend.tfvars`
- Environment variables: BWS_ACCESS_TOKEN, BW_PROJECT, KINFRA_LOG_DIR, KINFRA_LOG_LEVEL

## Recent Changes (2025-10-19)
- TerraformRunnerクラスのリファクタリングを実施
- executionパッケージを新規作成し、責務を分離
- ActionExecutor: アクション実行の共通パターン
- CommandInterpreter: コマンドライン引数の解釈
- SystemRequirement: システム要件のチェック
- UpdateHandler: アップデートのチェックと実行
- ConfigEditor: 設定ファイル編集機能
- DeploymentPipeline: デプロイパイプライン
- UpdateProcessor: アップデート処理