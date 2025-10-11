# kinfra

Kotlinベースの Terraform ラッパー。Bitwarden Secret Manager 統合により、複数環境でのシークレット管理を安全に実行します。

## 特徴

- 複数環境（dev/staging/prod）のTerraform操作を簡素化
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
kinfra init prod

# プラン実行
kinfra plan prod

# 適用
kinfra apply prod

# デプロイ（init + apply）
kinfra deploy prod

# リソース削除
kinfra destroy prod
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
./gradlew :app-cli:run --args="<command> <environment>"

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