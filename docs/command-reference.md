# コマンドリファレンス

kinfra CLIの全コマンドリファレンス。

## 基本構文

```bash
kinfra <command> [options]
```

---

## コマンド一覧

### help

ヘルプメッセージを表示。

```bash
kinfra help
kinfra --help
```

---

### hello

バージョン情報と対話型メニューを表示。

```bash
kinfra hello
```

**機能**:
- バージョン情報表示
- アップデートチェック
- Terraformコマンド実行（init/plan/apply）

---

### login

プロジェクトにログインし、`kinfra.yaml`を作成。

```bash
kinfra login
```

**動作**:
- Terraformディレクトリを検索
- `kinfra.yaml`を生成してプロジェクトルートに保存

---

### config

現在の設定を表示。

```bash
kinfra config
```

**表示内容**:
- プロジェクト設定
- ホスト設定

---

## Terraformコマンド

### init

Terraformを初期化。

```bash
kinfra init
```

**実行内容**: `terraform init`

---

### plan

実行計画を作成。

```bash
kinfra plan
```

**実行内容**: `terraform plan`

---

### apply

変更を適用。

```bash
kinfra apply
```

**実行内容**: `terraform apply -auto-approve`

**注意**: 自動承認されます。

---

### destroy

リソースを削除。

```bash
kinfra destroy
```

**実行内容**: `terraform destroy -auto-approve`

**警告**: リソースが完全に削除されます。

---

### deploy

init + plan + applyを連続実行。

```bash
kinfra deploy
```

**SDKモード**:
- `BWS_ACCESS_TOKEN`がある場合、自動的に`deploy-sdk`にリダイレクト
- Bitwarden Secret Managerからシークレットを取得して適用

---

### validate

設定ファイルを検証。

```bash
kinfra validate
```

**実行内容**: `terraform validate`

---

### fmt

設定ファイルをフォーマット。

```bash
kinfra fmt
```

**実行内容**: `terraform fmt`

---

## Bitwardenコマンド

### setup-r2

Cloudflare R2のTerraform Backendを設定（CLIベース）。

```bash
kinfra setup-r2
```

**前提条件**:
- `bw` CLIがインストール済み
- Bitwardenにログイン済み

**SDKモード**:
- `BWS_ACCESS_TOKEN`がある場合、自動的に`setup-r2-sdk`にリダイレクト

---

## 環境変数

### BWS_ACCESS_TOKEN

Bitwarden Secret Managerアクセストークン。

```bash
export BWS_ACCESS_TOKEN="your-token"
```

**効果**:
- SDKベースコマンドを有効化（`deploy-sdk`, `setup-r2-sdk`）
- `deploy` → `deploy-sdk`に自動リダイレクト
- `setup-r2` → `setup-r2-sdk`に自動リダイレクト

または、`.bws_token`ファイルに保存:

```bash
echo "your-token" > ~/.bws_token
```

---

### KINFRA_LOG_LEVEL

ログレベルを設定。

```bash
export KINFRA_LOG_LEVEL=DEBUG  # DEBUG, INFO, WARN, ERROR
```

デフォルト: `INFO`

---

### KINFRA_LOG_DIR

ログディレクトリを設定。

```bash
export KINFRA_LOG_DIR=/var/log/kinfra
```

デフォルト: `logs`

---

## 設定ファイル

### ~/.local/kinfra/project.json

プロジェクト設定。

```json
{
  "projectId": "abc123",
  "name": "my-project"
}
```

---

### ~/.local/kinfra/hosts.json

ホスト設定。

```json
{
  "hosts": [
    {
      "name": "web-server",
      "address": "192.168.1.10"
    }
  ]
}
```

---

### kinfra.yaml

プロジェクトルートに配置（`login`時に自動生成）。

```yaml
terraform_dir: ./terraform
environments:
  - dev
  - staging
  - prod
```

---

## 使用例

### 基本ワークフロー

```bash
# プロジェクトにログイン
kinfra login

# 初期化
kinfra init

# 計画確認
kinfra plan

# 適用
kinfra apply
```

### SDKモードでのデプロイ

```bash
# トークン設定
export BWS_ACCESS_TOKEN="your-token"

# デプロイ（自動的にSDKモードで実行）
kinfra deploy
```

### 設定確認

```bash
# 設定表示
kinfra config

# ヘルプ表示
kinfra help
```

---

## 終了コード

- `0`: 成功
- `1`: エラー

---

## トラブルシューティング

### コマンドが見つからない

PATHに`~/.local/bin`を追加:

```bash
export PATH="${HOME}/.local/bin:${PATH}"
```

### Terraformディレクトリが見つからない

`login`コマンドでプロジェクトを初期化:

```bash
kinfra login
```

### ログの確認

```bash
tail -f logs/kinfra.log
```