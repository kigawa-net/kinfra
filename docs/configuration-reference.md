# 設定リファレンス

kinfraの設定ファイルと環境変数のリファレンス。

## 📚 関連ドキュメント

- **[ドキュメントトップ](README.md)** - 全ドキュメントの一覧
- **[コマンドリファレンス](command-reference.md)** - CLIコマンドの詳細
- **[APIリファレンス](api-reference.md)** - Web APIの詳細
- **[SSH設定](ssh-configuration.md)** - SSH接続の設定方法

## 目次

- [環境変数](#環境変数)
  - [BWS_ACCESS_TOKEN](#bws_access_token)
  - [BW_PROJECT](#bw_project)
  - [KINFRA_LOG_LEVEL](#kinfra_log_level)
  - [KINFRA_LOG_DIR](#kinfra_log_dir)
- [設定ファイル](#設定ファイル)
  - [kinfra.kts / kinfra-parent.kts](#kinfrakts--kinfra-parentkts)
  - [~/.local/kinfra/project.json](#localkinfraprojectjson)
- [Terraform設定](#terraform設定)
- [ログ設定](#ログ設定)
  - [ログファイル](#ログファイル)
  - [ログ形式](#ログ形式)
  - [ログレベル](#ログレベル)
- [ディレクトリ構造](#ディレクトリ構造)
- [セキュリティ考慮事項](#セキュリティ考慮事項)
  - [シークレット管理](#シークレット管理)
  - [ファイルパーミッション](#ファイルパーミッション)
- [トラブルシューティング](#トラブルシューティング)
  - [設定ファイルが見つからない](#設定ファイルが見つからない)
  - [ログが出力されない](#ログが出力されない)
  - [環境変数が反映されない](#環境変数が反映されない)

## 環境変数

### BWS_ACCESS_TOKEN

**説明**: Bitwarden Secret Managerアクセストークン

**用途**: SDKベースコマンドの有効化

**設定方法**:

```bash
export BWS_ACCESS_TOKEN="your-access-token"
```

または、ファイルに保存:

```bash
echo "your-token" > ~/.bws_token
```

 **効果**:
 - `deploy` → `deploy-sdk`に自動リダイレクト

---

### BW_PROJECT

**説明**: BitwardenプロジェクトID

**用途**: Bitwardenプロジェクトの識別

**設定方法**:

```bash
export BW_PROJECT="project-uuid"
```

または、`.env`ファイル:

```bash
echo "BW_PROJECT=project-uuid" >> .env
```

---

### KINFRA_LOG_LEVEL

**説明**: ログレベル

**デフォルト**: `INFO`

**指定可能な値**: `DEBUG`, `INFO`, `WARN`, `ERROR`

**設定方法**:

```bash
export KINFRA_LOG_LEVEL=DEBUG
```

---

### KINFRA_LOG_DIR

**説明**: ログディレクトリパス

**デフォルト**: `logs`

**設定方法**:

```bash
export KINFRA_LOG_DIR=/var/log/kinfra
```

---

## 設定ファイル

### kinfra.kts / kinfra-parent.kts

**場所**: プロジェクトルート

**生成方法**: `kinfra login`コマンドで自動生成（`kinfra config edit`でサンプルからも生成可能）

**形式**: Kotlinスクリプト（`.kts`）。実行時にkinfraがコンパイル・評価する、型安全な設定DSL。
`kinfra.kts`はプロジェクト単位（サブプロジェクトも含む）のTerraform設定、`kinfra-parent.kts`は
複数プロジェクトをまとめる親設定を表す。

**例** (`kinfra-parent.kts`):

```kotlin
projectName = "my-infrastructure"
description = "Parent project for managing multiple infrastructure components"

terraform {
    version = "1.5.0"
    workingDirectory = "."
    generateOutputDir = "./generated"  // generateコマンドの出力ディレクトリ

    backendConfig {
        bucket = "terraform-state"
        key = "project/terraform.tfstate"
        region = "auto"
        // bws(...)はBitwarden Secret Managerのシークレットキーを参照する。
        // 実際にterraformを呼び出す直前に解決される（手動exportは不要）
        accessKey = bws("r2-access-key-id")
        secretKey = bws("r2-secret-access-key")
    }

    variable("cloudflare_api_token", "cloudflare-api-token")
    variable("aws_access_key", "aws-access-key")
}

subProjects {
    subProject("project-a")
    subProject("project-b", path = "../project-b")
}

bitwarden {
    projectId = "your-bitwarden-project-id"
}

update {
    autoUpdate = true
    checkInterval = 86400000  // 24 hours in milliseconds
    githubRepo = "kigawa-net/kinfra"
}
```

**例** (`kinfra.kts`、サブプロジェクトなど単一プロジェクトの場合):

```kotlin
projectId = "my-infrastructure"
description = "Parent project for managing multiple infrastructure components"

terraform {
    version = "1.5.0"
    workingDirectory = "."
}

bitwarden {
    projectId = "your-bitwarden-project-id"
}
```

**DSLリファレンス**:

| トップレベル | 説明 |
|-----------|------|
| `projectName` (parent) / `projectId` (project) | プロジェクト名/ID |
| `description` | プロジェクト説明（任意） |
| `terraform { }` | Terraform設定ブロック |
| `subProjects { }` | サブプロジェクトのリスト（`kinfra-parent.kts`のみ） |
| `bitwarden { }` | Bitwarden設定ブロック |
| `update { }` | 自動更新設定ブロック |
| `bws("secret-key")` | Bitwarden Secret Managerのシークレットを参照する関数。どの設定値の中でも使える |

#### terraform { } ブロック

| フィールド | 型 | 説明 | デフォルト |
|-----------|-----|------|----------|
| `version` | String | Terraformバージョン | `""` |
| `workingDirectory` | String | 作業ディレクトリ | `"."` |
| `generateOutputDir` | String? | generateコマンドの出力ディレクトリ | `null`（カレントディレクトリ） |
| `backendConfig { }` | ブロック | `-backend-config`としてterraformに渡す設定 | なし |
| `variable(terraformVariable, bitwardenSecretKey)` | 関数 | Bitwardenシークレット→Terraform変数のマッピングを追加 | なし |
| `output(terraformOutput, bitwardenSecretKey)` | 関数 | Terraform出力→Bitwardenシークレットのマッピングを追加 | なし |

**variable(...)** で登録したマッピングに基づき、plan/apply実行時に`secrets.tfvars`が自動生成され、Terraform変数として使用される。

#### backendConfig { } ブロック

| フィールド | 型 | 説明|
|-----------|-----|------|
| `bucket` | String? | ステートを保存するバケット名 |
| `key` | String? | ステートファイルのキー（パス） |
| `region` | String? | リージョン（R2の場合は`"auto"`） |
| `endpoint` | String? | S3互換エンドポイントURL |
| `accessKey` | String? | アクセスキー（`bws(...)`推奨） |
| `secretKey` | String? | シークレットキー（`bws(...)`推奨） |
| `set(key, value)` | 関数 | 上記以外の任意の`-backend-config`キーを追加 |

#### subProjects { } ブロック（kinfra-parent.ktsのみ）

| 関数 | 説明 |
|------|------|
| `subProject(name, path = name)` | サブプロジェクトを追加する。`path`省略時は`name`と同じ |

#### bitwarden { } / update { } ブロック

| フィールド | 型 | 説明 | デフォルト |
|-----------|-----|------|----------|
| `bitwarden.projectId` | String | BitwardenプロジェクトID | `""` |
| `update.autoUpdate` | Boolean | 自動更新有効 | `true` |
| `update.checkInterval` | Long | チェック間隔（ミリ秒） | 86400000（24時間） |
| `update.githubRepo` | String | GitHubリポジトリ | `"kigawa-net/kinfra"` |

**注意（既存プロジェクトの移行）**: 以前のYAML形式（`kinfra.yaml`/`kinfra-parent.yaml`）からの
自動変換は提供しない。`.kts`ファイルを新規に作成し、上記DSLで書き直す必要がある。

**プログラム的な変更の挙動**: `kinfra sub add`/`kinfra sub remove`/`kinfra config add-subproject`など
CLIから設定を変更するコマンドは、`.kts`ファイルを評価して得た設定オブジェクトを変更し、
正規化されたKotlinコードとしてファイル全体を再生成する。手書きで追加したコメントや、
DSLの範囲を超える独自のKotlinロジックがある場合、これらのコマンドを実行すると
上書きされる点に注意（YAML時代の`saveData()`も同様にファイル全体を再生成していたため、
既存の制約を引き継いだ形になる）。

### generateOutputDir設定

**説明**: `kinfra current generate variable`コマンドで生成されるファイルの出力ディレクトリを指定します。

**優先順位**:
1. `--output-dir` / `-o` CLIフラグ (最高優先度)
2. `kinfra.kts`の`terraform.generateOutputDir`設定
3. カレントディレクトリ (フォールバック)

**使用例**:

```kotlin
// kinfra.kts
terraform {
    generateOutputDir = "./generated/variables"
}
```

```bash
# CLIフラグで上書き
kinfra current generate variable --output-dir /tmp/variables

# 設定を使用
kinfra current generate variable

# カレントディレクトリに出力 (設定なしの場合)
kinfra current generate variable
```

**注意**: 相対パスの場合はプロジェクトルートからの相対パスとして解釈されます。

---

### ~/.local/kinfra/project.json

**場所**: `~/.local/kinfra/project.json`

**生成方法**: 自動生成

**形式**: JSON

**例**:

```json
{
  "projectId": "abc123",
  "name": "my-project",
  "description": "Project description"
}
```

**フィールド**:

| フィールド | 型 | 説明 |
|-----------|-----|------|
| projectId | string | プロジェクトID |
| name | string | プロジェクト名 |
| description | string | プロジェクト説明（任意） |

---

 ## Terraform設定

```hcl
terraform {
  backend "s3" {
    bucket = "terraform-state"
    key    = "project/terraform.tfstate"
    region = "auto"

    endpoints = {
      s3 = "https://<account-id>.r2.cloudflarestorage.com"
    }

    skip_credentials_validation = true
    skip_region_validation      = true
    skip_requesting_account_id  = true
  }
}
```

**必要な認証情報**: R2のアクセスキー/シークレットキー

上記HCLの`accessKey`/`secretKey`相当は、`kinfra-parent.kts`/`kinfra.kts`の`backendConfig { }`内で
`bws("secret-key")`を使ってBitwarden Secret Managerから参照できる（[設定ファイル](#kinfrakts--kinfra-parentkts)参照）。
`init`/`plan`/`apply`/`destroy`/`show`実行時にkinfraが自動的に解決し、`-backend-config`として
terraformに渡す（手動での`export`は不要）。`BWS_ACCESS_TOKEN`（SDKモード）が設定されていない場合、
`bws(...)`の値は未解決のまま渡される。

---

## ログ設定

### ログファイル

**デフォルトパス**: `logs/kinfra.log`

**カスタムパス**:

```bash
export KINFRA_LOG_DIR=/var/log/kinfra
```

### ログ形式

```
2025-10-11 10:30:45 [INFO] TerraformService: Executing init for dev
2025-10-11 10:30:46 [DEBUG] ProcessExecutor: Running command: terraform init
2025-10-11 10:30:50 [INFO] TerraformService: Init completed successfully
```

### ログレベル

| レベル | 説明 |
|-------|------|
| DEBUG | デバッグ情報を含むすべてのログ |
| INFO | 一般的な情報ログ |
| WARN | 警告メッセージ |
| ERROR | エラーメッセージのみ |

---

## ディレクトリ構造

```
~/.local/kinfra/
├── kinfra.jar           # アプリケーション本体
└── project.json         # プロジェクト設定

~/.local/bin/
└── kinfra               # 実行スクリプト

<project-root>/
├── kinfra.kts           # プロジェクト設定
├── kinfra-parent.kts    # 親プロジェクト設定（マルチプロジェクトの場合）
├── terraform/           # Terraformファイル
└── logs/                # ログディレクトリ
    └── kinfra.log
```

---

## セキュリティ考慮事項

### シークレット管理

**推奨**:
- `BWS_ACCESS_TOKEN`を環境変数またはセキュアなファイル（`.bws_token`）で管理
- Gitリポジトリにシークレットをコミットしない

**`.gitignore`に追加**:

```
.bws_token
.env
kinfra.kts
kinfra-parent.kts
logs/
```

### ファイルパーミッション

```bash
# シークレットファイルのパーミッション
chmod 600 ~/.bws_token

# 設定ファイルのパーミッション
chmod 644 ~/.local/kinfra/project.json
```

---

## トラブルシューティング

### 設定ファイルが見つからない

```bash
# プロジェクトにログイン
kinfra login

# 設定を確認
kinfra config
```

### ログが出力されない

```bash
# ログディレクトリの確認
ls -la logs/

# ログディレクトリを作成
mkdir -p logs

# パーミッションの確認
chmod 755 logs
```

### 環境変数が反映されない

```bash
# 環境変数の確認
env | grep KINFRA

# シェル再起動
source ~/.bashrc  # または source ~/.zshrc
```