# kinfra

Kotlinベースの Terraform ラッパー。Bitwarden Secret Manager 統合により、シークレットを安全に管理します。

## 目次

- [特徴](#特徴)
- [インストール](#インストール)
  - [ワンライナーインストール](#ワンライナーインストール)
  - [インストール先](#インストール先)
  - [PATHの設定](#pathの設定)
- [使い方](#使い方)
  - [CLI](#cli)
  - [Web API](#web-api)
  - [GitHub Actions](#github-actions)
- [環境変数](#環境変数)
- [開発](#開発)
  - [要件](#要件)
  - [ビルド](#ビルド)
- [アーキテクチャ](#アーキテクチャ)
- [ライセンス](#ライセンス)

## 特徴

- Terraform操作を簡素化
- Bitwarden Secret Managerとの統合によるシークレット管理
- CLI と Web API の2つのインターフェース
- モジュラーアーキテクチャによる保守性の高い設計

## インストール

### ワンライナーインストール

```bash
curl -fsSL https://raw.githubusercontent.com/kigawa-net/kinfra/main/install.sh | bash
```

特定のバージョンをインストール:

```bash
curl -fsSL https://raw.githubusercontent.com/kigawa-net/kinfra/main/install.sh | bash -s v0.0.1
```

### インストール先

- **実行可能ファイル**: `~/.local/bin/kinfra`
- **アプリケーション**: `~/.local/kinfra/kinfra.jar`

### PATHの設定

`~/.local/bin` がPATHに含まれていない場合、以下をシェル設定ファイル（`~/.bashrc` または `~/.zshrc`）に追加してください:

```bash
export PATH="${HOME}/.local/bin:${PATH}"
```

設定後、シェルを再読み込み:

```bash
source ~/.bashrc  # または source ~/.zshrc
```

## 使い方

### CLI

```bash
# ヘルプを表示
kinfra --help

# Terraform初期化
kinfra init

# プラン実行
kinfra plan

# 適用
kinfra apply

# デプロイ（init + plan + apply）
kinfra deploy

# リソース削除
kinfra destroy
```

### Web API

```bash
# Webサーバー起動
./gradlew :app-web:run

# または Shadow JAR から起動
java -jar app-web/build/libs/kinfra-web-*.jar
```

APIエンドポイント:
- `GET /` - APIステータス
- `GET /health` - ヘルスチェック
- `POST /terraform/init` - Terraform初期化
- `POST /terraform/plan` - Terraform実行計画
- `POST /terraform/apply` - リソース作成
- `POST /terraform/destroy` - リソース削除

### GitHub Actions

KInfraはGitHub Actionとして使用できます。

#### 基本的な使い方

```yaml
name: Deploy Infrastructure

on:
  push:
    branches:
      - main

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Deploy with KInfra
        uses: kigawa-net/kinfra@v1
        with:
          command: deploy
          working-directory: ./terraform
          bws-access-token: ${{ secrets.BWS_ACCESS_TOKEN }}
          bw-project: ${{ secrets.BW_PROJECT }}
```

#### 利用可能なインプット

| インプット | 必須 | デフォルト | 説明 |
|----------|------|-----------|------|
| `command` | はい | - | 実行するTerraformコマンド (init, plan, apply, deploy, destroy, validate, format) |
| `working-directory` | いいえ | `.` | Terraformファイルがあるディレクトリ |
| `bws-access-token` | いいえ | - | Bitwarden Secret Managerアクセストークン |
| `bw-project` | いいえ | - | BitwardenプロジェクトID |
| `log-level` | いいえ | `INFO` | ログレベル (DEBUG, INFO, WARN, ERROR) |
| `java-version` | いいえ | `21` | 使用するJavaバージョン |

#### 出力

| 出力 | 説明 |
|------|------|
| `exit-code` | KInfraコマンドの終了コード |

#### より詳細な例

```yaml
name: Terraform Workflow

on:
  pull_request:
    branches:
      - main

jobs:
  plan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Terraform Plan
        uses: kigawa-net/kinfra@v1
        with:
          command: plan
          working-directory: ./terraform
          bws-access-token: ${{ secrets.BWS_ACCESS_TOKEN }}
          bw-project: ${{ secrets.BW_PROJECT }}
          log-level: DEBUG

  apply:
    runs-on: ubuntu-latest
    needs: plan
    if: github.event_name == 'push'
    steps:
      - uses: actions/checkout@v4

      - name: Terraform Apply
        uses: kigawa-net/kinfra@v1
        with:
          command: apply
          working-directory: ./terraform
          bws-access-token: ${{ secrets.BWS_ACCESS_TOKEN }}
          bw-project: ${{ secrets.BW_PROJECT }}
```


## 環境変数

- `BWS_ACCESS_TOKEN` - Bitwarden Secret Managerアクセストークン
- `BW_PROJECT` - BitwardenプロジェクトID
- `KINFRA_LOG_DIR` - ログディレクトリパス（デフォルト: "logs"）
- `KINFRA_LOG_LEVEL` - ログレベル: DEBUG、INFO、WARN、ERROR（デフォルト: INFO）

## 開発

### 要件

- JDK 21
- Gradle 8.10.2
- Kotlin 2.2.0

### ビルド

```bash
# プロジェクトのビルド
./gradlew build

# テスト実行
./gradlew test

# CLIアプリケーション実行
./gradlew :app-cli:run --args="<command>"

# Shadow JAR作成
./gradlew :app-cli:shadowJar
```

## アーキテクチャ

プロジェクトはマルチモジュール構成:

- **model** - ドメインモデルとインターフェース
- **action** - ビジネスロジックインターフェース
- **infrastructure** - 実装層
- **app-cli** - CLIアプリケーション
- **app-web** - Webアプリケーション

詳細は [CLAUDE.md](./CLAUDE.md) を参照してください。

## ライセンス

MIT License