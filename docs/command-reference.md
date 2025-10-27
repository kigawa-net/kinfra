# コマンドリファレンス

kinfra CLIの全コマンドリファレンス。

## 📚 関連ドキュメント

- **[ドキュメントトップ](README.md)** - 全ドキュメントの一覧
- **[APIリファレンス](api-reference.md)** - Web APIの詳細
- **[設定リファレンス](configuration-reference.md)** - 設定オプションの詳細
- **[Terraform使用方法](terraform-usage.md)** - Terraform実行の詳細ガイド

## 目次

- [基本構文](#基本構文)
- [コマンド一覧](#コマンド一覧)
  - [help](#help)
  - [hello](#hello)
  - [login](#login)
- [サブプロジェクトコマンド](#サブプロジェクトコマンド)
  - [sub list](#sub-list)
  - [sub add](#sub-add)
  - [sub plan](#sub-plan)
- [Currentコマンド](#currentコマンド)
  - [current generate variable](#current-generate-variable)
  - [current plan](#current-plan)
- [Terraformコマンド](#terraformコマンド)
  - [init](#init)
  - [plan](#plan)
  - [apply](#apply)
  - [destroy](#destroy)
  - [deploy](#deploy)
  - [validate](#validate)
  - [fmt](#fmt)
- [Bitwardenコマンド](#bitwardenコマンド)
- [環境変数](#環境変数)
  - [BWS_ACCESS_TOKEN](#bws_access_token)
  - [KINFRA_LOG_LEVEL](#kinfra_log_level)
  - [KINFRA_LOG_DIR](#kinfra_log_dir)
- [設定ファイル](#設定ファイル)
  - [~/.local/kinfra/project.json](#localkinfraprojectjson)
  - [kinfra.yaml](#kinfrayaml)
- [使用例](#使用例)
  - [基本ワークフロー](#基本ワークフロー)
  - [SDKモードでのデプロイ](#sdkモードでのデプロイ)
  - [設定確認](#設定確認)
- [終了コード](#終了コード)
- [トラブルシューティング](#トラブルシューティング)
  - [コマンドが見つからない](#コマンドが見つからない)
  - [Terraformディレクトリが見つからない](#terraformディレクトリが見つからない)
  - [ログの確認](#ログの確認)

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

## サブプロジェクトコマンド

### sub list

kinfra-parent.yamlに設定されたサブプロジェクトの一覧を表示。

```bash
kinfra sub list
```

**実行内容**:
- kinfra-parent.yamlからサブプロジェクト一覧を取得
- 番号付きで表示

---

### sub add

kinfra-parent.yamlに新しいサブプロジェクトを追加。

```bash
kinfra sub add <project-name>
```

**実行内容**:
- 指定したプロジェクト名をkinfra-parent.yamlのsubProjectsに追加
- 設定ファイルが存在しない場合は新規作成

**例**:
```bash
kinfra sub add my-new-project
```

---

### sub plan

指定したサブプロジェクトでterraform planを実行。

```bash
kinfra sub plan <sub-project-name>
```

**実行内容**:
- 指定したサブプロジェクトのディレクトリに移動
- `terraform plan`を実行

**例**:
```bash
kinfra sub plan my-project
```

---

## Currentコマンド

### current generate variable

現在のディレクトリにTerraform変数を生成します。

```bash
kinfra current generate variable [options] [variable_name]
```

**オプション**:
- `--output-dir, -o <dir>`: 出力ディレクトリを指定
- `--with-outputs`: 対応するoutputs.tfも生成

**動作**:
- kinfra.yamlまたはkinfra-parent.yamlからvariableMappingsを読み込み
- 引数なし: 全ての変数を生成
- 引数あり: 指定した変数のみを生成
- 出力ディレクトリの優先順位: CLIオプション > kinfra.yaml設定 > カレントディレクトリ

**設定例 (kinfra.yaml)**:
```yaml
project:
  terraform:
    generateOutputDir: /path/to/output
    variableMappings:
      - terraformVariable: "my_var"
        bitwardenSecret: "my_secret"
```

**使用例**:
```bash
# 全ての変数を生成
kinfra current generate variable

# 特定の変数を生成
kinfra current generate variable my_var

# 出力と共に生成
kinfra current generate variable --with-outputs

# 特定ディレクトリに出力
kinfra current generate variable --output-dir /tmp
```

---

### current plan

現在のディレクトリでTerraform planを実行します。

```bash
kinfra current plan [terraform_options]
```

**動作**:
- カレントディレクトリにTerraformファイルがあるか確認
- kinfra.yamlまたはkinfra-parent.yamlからbackendConfigを読み込み
- 自動でterraform initを実行
- backendConfigをterraformコマンドに適用
- terraform planを実行

**設定例 (kinfra.yaml)**:
```yaml
project:
  terraform:
    backendConfig:
      bucket: "my-terraform-state-bucket"
      key: "terraform.tfstate"
      region: "us-east-1"
```

**使用例**:
```bash
# 基本plan
kinfra current plan

# 追加オプション付き
kinfra current plan -out=myplan.tfplan
```

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

**実行内容**:
1. R2バックエンド設定の確認・作成
2. `terraform init`
3. `terraform plan`
4. `terraform apply -auto-approve`
5. **デプロイ成功後、自動的に`git push`を実行**

**SDKモード**:
- `BWS_ACCESS_TOKEN`がある場合、自動的に`deploy-sdk`にリダイレクト
- Bitwarden Secret Managerからシークレットを取得して適用

**注意**:
- デプロイが成功した場合のみ、自動的にリモートリポジトリへプッシュされます
- git pushが失敗してもデプロイの結果には影響しません（警告として表示）
- プッシュしたくない場合は、事前にコミットしていないことを確認してください

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



## 環境変数

### BWS_ACCESS_TOKEN

Bitwarden Secret Managerアクセストークン。

```bash
export BWS_ACCESS_TOKEN="your-token"
```

 **効果**:
 - SDKベースコマンドを有効化（`deploy-sdk`）
 - `deploy` → `deploy-sdk`に自動リダイレクト

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