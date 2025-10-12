# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 目次

- [プロジェクト概要](#プロジェクト概要)
- [ビルド: Gradle 8.10.2 / Java 21 / Kotlin 2.2.0](#ビルド-gradle-8102--java-21--kotlin-220)
- [アーキテクチャ: レイヤードマルチモジュール](#アーキテクチャ-レイヤードマルチモジュール)
  - [DI（Koin）](#dikoin)
  - [コマンドシステム](#コマンドシステム)
  - [UI: ANSIカラー](#ui-ansiカラー)
- [設定](#設定)
- [Web API (Ktor, :8080)](#web-api-ktor-8080)
- [実装ノート](#実装ノート)

## プロジェクト概要

KotlinベースのTerraformラッパー。Bitwarden Secret Manager統合によりシークレットを安全に管理。
- **CLI** (`app-cli`) - コマンドラインツール
- **Web** (`app-web`) - Ktor REST API

## ビルド: Gradle 8.10.2 / Java 21 / Kotlin 2.2.0

```bash
./gradlew build                                    # ビルド
./gradlew test                                     # 全テスト
./gradlew :app-cli:run --args="<cmd>"             # CLI実行
./gradlew :app-web:run                             # Web実行
./gradlew test --tests "net.kigawa.kinfra.Foo"    # 単一テスト
```

## アーキテクチャ: レイヤードマルチモジュール

```
model (依存なし) → action (契約) → infrastructure (実装) → app-cli / app-web
```

- **model**: ドメインモデル（`Command`, `CommandType`, `TerraformConfig`など）
- **action**: ビジネスロジック契約（`TerraformService`）
- **infrastructure**: 実装（Terraform/Bitwarden統合、プロセス実行、設定管理、ログ）
- **app-cli**: CLI（`App.kt`, `TerraformRunner`, `commands/`, Koin DI）
- **app-web**: Ktor REST API（`Application.kt`, `/terraform/*`エンドポイント）

### DI（Koin）

- **CLI**: `di/AppModule.kt` - コマンドを`CommandType.commandName`で名前付き登録。`BWS_ACCESS_TOKEN`があればSDK版コマンドを登録（`deploy`→`deploy-sdk`自動リダイレクト）
- **Web**: `di/WebModule.kt` - Infrastructure実装を提供

### コマンドシステム

新規コマンド追加：
1. `CommandType` enumに追加（`model/.../CommandType.kt`）
2. `Command`実装クラス作成（`app-cli/.../commands/`）
3. `di/AppModule.kt`で登録
4. Terraformチェック不要なら`TerraformRunner.kt`の`skipTerraformCheck`に追加

**重要**: `deploy`/`setup-r2`はSDK版に自動リダイレクト。

### UI: ANSIカラー

全コマンドで`AnsiColors`クラス（`app-cli/util/AnsiColors.kt`）を使用。`${AnsiColors.RED}`等でカラー出力。

## 設定

**ファイル配置**: `~/.local/kinfra/` - `project.json`, `hosts.json` / プロジェクトルート - `kinfra.yaml`（`login`時自動生成）、`backend.tfvars`（バックエンド設定）

**環境変数**:
- `BWS_ACCESS_TOKEN` - Bitwarden Secret Manager（または`.bws_token`）。SDKコマンド有効化
- `BW_PROJECT` - プロジェクトID（`.env`）
- `KINFRA_LOG_DIR` / `KINFRA_LOG_LEVEL` - ログ設定（デフォルト: "logs" / INFO）

**シークレット管理**: CLIベース（`bw`）とSDKベース（直接SDK）。`BWS_ACCESS_TOKEN`があればSDK優先。

## Web API (Ktor, :8080)

`POST /terraform/{init,plan,apply,destroy,validate,format}` - リクエスト: `{"command":"apply"}`

## 実装ノート

- **エラー**: `ConfigRepository`のYAML読込は例外スロー。呼出側でハンドル
- **シリアライゼーション**: JSON=Gson、YAML=kotlinx-serialization+kaml。modelの`@Serializable`必須
- **設定管理**: `ConfigRepositoryImpl`は`hosts.json`(Gson)、`project.json`(Gson)、`kinfra.yaml`(kaml)を管理
- **環境管理**: 環境（prod/dev等）の概念は削除済み。全てのコマンドは環境パラメータなしで動作