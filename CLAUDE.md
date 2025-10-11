# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

kinfraは、Bitwarden Secret Manager統合を備えたKotlinベースのTerraformラッパーCLIです。複数の環境（dev/staging/prod）における一般的なTerraform操作を簡素化し、シークレットを安全に管理します。

## ビルドシステム

- **ビルドツール**: Gradle 8.10.2（Kotlin DSL使用）
- **JDKバージョン**: Java 21
- **Kotlinバージョン**: 2.2.0

### 共通コマンド

```bash
# プロジェクトのビルド
./gradlew build

# テスト実行
./gradlew test

# Shadow JAR作成（全依存関係を含むuber-jar）
./gradlew shadowJar

# アプリケーション実行（開発モード）
./gradlew run --args="<command> <environment>"

# Shadow JARを使用して実行
./gradlew runShadow --args="<command> <environment>"

# ビルド成果物のクリーンアップ
./gradlew clean
```

### テスト実行

```bash
# 全テスト実行
./gradlew test

# 特定モジュールのテスト実行
./gradlew :model:test
./gradlew :action:test
./gradlew :infrastructure:test
./gradlew :app:test

# 単一のテストクラス実行
./gradlew test --tests "net.kigawa.kinfra.SpecificTestClass"
```

## アーキテクチャ

### マルチモジュール構成

プロジェクトは4つのGradleモジュールに分かれたレイヤードアーキテクチャを採用：

1. **model** - ドメインモデルとインターフェース（依存なし）
   - データクラス: `Environment`, `Command`, `TerraformConfig`, `CommandResult`など
   - Enum: `CommandType`
   - 純粋なKotlin、外部依存なし

2. **action** - ビジネスロジックインターフェース（modelに依存）
   - `TerraformService` - コアTerraform操作インターフェース
   - `EnvironmentValidator` - 環境検証インターフェース
   - インフラストラクチャ層の契約を定義

3. **infrastructure** - 実装層（model、actionに依存）
   - `TerraformServiceImpl` - Terraformコマンド実行
   - `BitwardenRepositoryImpl` - Bitwarden CLI統合
   - `BitwardenSecretManagerRepositoryImpl` - Bitwarden SDK統合
   - `ProcessExecutor` - システムプロセス実行
   - `FileRepository` - ファイルシステム操作
   - `ConfigRepository` - 設定管理
   - `TerraformVarsManager` - Terraform変数ファイル管理
   - `Logger` - ファイルベースのログシステム

4. **app** - プレゼンテーション層（すべてに依存）
   - エントリーポイント: `App.kt`
   - `TerraformRunner` - コマンドルーティングと実行
   - `commands/`パッケージ内のコマンド実装
   - `di/AppModule.kt`でのKoinによる依存性注入設定

### 依存性注入

プロジェクトはKoinを使用しています。すべてのモジュールバインディングは`app/src/main/kotlin/net/kigawa/kinfra/di/AppModule.kt`で設定されています。

重要な点：
- SDKベースのコマンド（`deploy-sdk`, `setup-r2-sdk`）は`BWS_ACCESS_TOKEN`が利用可能な場合のみ条件付きで登録される
- コマンドは`CommandType.commandName`を使用して名前付きシングルトンとして登録される
- `deploy`と`setup-r2`コマンドは`TerraformRunner.kt:43-52`で自動的にSDK版にリダイレクトされる

### コマンドシステム

コマンドは一貫したパターンに従います：
- 各コマンドは`Command`インターフェースを実装
- コマンドは`CommandType` enumに登録
- `TerraformRunner`がコマンドのルーティング、検証、実行を処理
- 環境を必要とするコマンド（init、plan、apply、destroy、deploy）は、環境が指定されていない場合、自動的に"prod"をデフォルトとして使用（`TerraformRunner.kt:95-105`を参照）

## 設定

### 環境変数

- `BWS_ACCESS_TOKEN` - Bitwarden Secret Managerアクセストークン（`.bws_token`ファイルにも保存可能）
- `BW_PROJECT` - BitwardenプロジェクトID（`.env`ファイルに保存）
- `KINFRA_LOG_DIR` - ログディレクトリパス（デフォルト: "logs"）
- `KINFRA_LOG_LEVEL` - ログレベル: DEBUG、INFO、WARN、ERROR（デフォルト: INFO）

### Terraformドキュメント

`docs/`ディレクトリには以下の詳細なドキュメント（日本語）が含まれています：
- `structure.md` - リポジトリ構造とTerraformの組織化
- `terraform-usage.md` - Terraform実行スクリプト
- `ssh-configuration.md` - SSH設定とセキュリティ
- `kubernetes.md` - Kubernetesマニフェスト管理
- `node-exporter.md` - Prometheus Node Exporter設定

## シークレット管理

プロジェクトはBitwarden統合に2つのアプローチをサポート：

1. **CLIベース**（`BitwardenRepositoryImpl`） - `bw` CLIコマンドを使用
2. **SDKベース**（`BitwardenSecretManagerRepositoryImpl`） - Bitwarden SDKを直接使用

`BWS_ACCESS_TOKEN`が設定されている場合、SDKアプローチが優先され、デフォルトで有効になります。SDK統合を使用するコマンド：
- `deploy-sdk`（`deploy`経由でアクセス）
- `setup-r2-sdk`（`setup-r2`経由でアクセス）

## モジュール依存関係

```
app
├── model
├── action
│   └── model
└── infrastructure
    ├── model
    └── action

buildSrc（規約プラグイン）
├── kinfra-common.gradle.kts - 全モジュール共通設定
└── kinfra-root.gradle.kts - ルートプロジェクト設定
```

## テスト

すべてのモジュールはJUnit 5を使用してテストします。テスト依存関係は`kinfra-common`規約プラグインで設定されています。

## ロギング

アプリケーションはカスタムファイルベースのログシステム（`FileLogger`）を使用：
- `KINFRA_LOG_DIR`で指定されたディレクトリにログを出力（デフォルト: "logs"）
- `KINFRA_LOG_LEVEL`経由で設定可能なログレベルをサポート
- 各実行ごとにタイムスタンプ付きログファイルを作成
- Koin DIを通じてアプリケーション全体で利用可能