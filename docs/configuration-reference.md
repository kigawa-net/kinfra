# 設定リファレンス

kinfraの設定ファイルと環境変数のリファレンス。

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

### kinfra.yaml

**場所**: プロジェクトルート

**生成方法**: `kinfra login`コマンドで自動生成

**形式**: YAML

**例** (新しい形式):

```yaml
project:
  projectId: "my-infrastructure"
  description: "Parent project for managing multiple infrastructure components"
  terraform:
    version: "1.5.0"
    workingDirectory: "."

bitwarden:
  projectId: "your-bitwarden-project-id"

subProjects:
  - projectId: "project-a"
    description: "First sub-project"
    terraform:
      version: "1.5.0"
      workingDirectory: "./project-a"
  - projectId: "project-b"
    description: "Second sub-project"

update:
  autoUpdate: true
  checkInterval: 86400000  # 24 hours in milliseconds
  githubRepo: "kigawa-net/kinfra"
```

**例** (古い形式、互換性維持):

```yaml
rootProject:
  projectId: "my-infrastructure"
  description: "Parent project for managing multiple infrastructure components"
  terraform:
    version: "1.5.0"
    workingDirectory: "."

bitwarden:
  projectId: "your-bitwarden-project-id"

subProjects:
  - name: "project-a"  # 古い形式では 'name' を使用
    description: "First sub-project"
    terraform:
      version: "1.5.0"
      workingDirectory: "./project-a"

update:
  autoUpdate: true
  checkInterval: 86400000
  githubRepo: "kigawa-net/kinfra"
```

**フィールド**:

| フィールド | 型 | 説明 |
|-----------|-----|------|
| project | object | ルートプロジェクト設定 (推奨) |
| rootProject | object | ルートプロジェクト設定 (古い形式、互換性維持) |
| bitwarden | object | Bitwarden設定 |
| subProjects | array | サブプロジェクトのリスト |
| update | object | 自動更新設定 |

#### プロジェクト設定 (project/rootProject)

| フィールド | 型 | 説明 |
|-----------|-----|------|
| projectId | string | プロジェクトID (新しい形式) |
| name | string | プロジェクト名 (古い形式、互換性維持) |
| description | string | プロジェクト説明 (任意) |
| terraform | object | Terraform設定 |

#### Terraform設定

| フィールド | 型 | 説明 | デフォルト |
|-----------|-----|------|----------|
| version | string | Terraformバージョン | "1.5.0" |
| workingDirectory | string | 作業ディレクトリ | "." |

#### Bitwarden設定

| フィールド | 型 | 説明 |
|-----------|-----|------|
| projectId | string | BitwardenプロジェクトID |

#### 更新設定

| フィールド | 型 | 説明 | デフォルト |
|-----------|-----|------|----------|
| autoUpdate | boolean | 自動更新有効 | true |
| checkInterval | number | チェック間隔 (ミリ秒) | 86400000 (24時間) |
| githubRepo | string | GitHubリポジトリ | "kigawa-net/kinfra" |

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

**必要なシークレット**:
- `AWS_ACCESS_KEY_ID`: R2アクセスキー
- `AWS_SECRET_ACCESS_KEY`: R2シークレットキー

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
├── kinfra.yaml          # プロジェクト設定
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
kinfra.yaml
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