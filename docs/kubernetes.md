# Kubernetes設定とマニフェスト

このドキュメントは、Kubernetesマニフェストの管理と適用方法について詳しく説明します。

## 📚 関連ドキュメント

- **[ドキュメントトップ](README.md)** - 全ドキュメントの一覧
- **[Terraform使用方法](terraform-usage.md)** - Terraformでのデプロイ方法
- **[SSH設定](ssh-configuration.md)** - リモート適用のためのSSH設定
- **[Node Exporter](node-exporter.md)** - 監視対象のNode Exporter設定
- **[構造](structure.md)** - リポジトリの構造と組織化

## 概要

このインフラストラクチャでは、Prometheusエコシステムに関連するKubernetesリソースを管理します。マニフェストは`kubernetes/manifests/`ディレクトリに配置され、Terraformを通じて適用されます。

## マニフェスト一覧

### 1. ingress.yml
**用途**: Prometheus GrafanaへのHTTPSアクセスを提供
**リソース**: Kubernetes Ingress
**主な機能**:
- TLS終端
- 証明書管理
- パスベースルーティング

### 2. prometheus.yml
**用途**: Prometheusスタック全体のデプロイ
**リソース**: Argo CD Application
**主な機能**:
- Helmチャートベースのデプロイ
- GitOpsワークフロー
- 自動同期とセルフヒーリング

### 3. pve-exporter.yml
**用途**: Proxmox VEメトリクスの収集
**リソース**: Deployment + Service
**主な機能**:
- Proxmox APIからのメトリクス取得
- PrometheusスクレイピングターゲットとしてのServiceエクスポート

### 4. nginx-exporter.yml
**用途**: Nginxメトリクスの収集
**リソース**: Deployment
**状態**: デフォルトで無効（コメントアウト）
**注意**: `apply_nginx_exporter=true`で有効化

## 適用方法

### 1. SSH+kubectl方式（デフォルト）

リモートホストにSSH接続してkubectlコマンドを実行します。

**設定変数**:
```hcl
use_ssh_kubectl = true
target_host = "192.168.1.120"  # k8s4サーバー
ssh_user = "kigawa"
ssh_key_path = "~/.ssh/key/id_ed25519"
remote_manifests_dir = "/tmp/k8s-manifests"
remote_kubectl_context = ""  # 現在のコンテキストを使用
```

**プロセス**:
1. マニフェストファイルをリモートホストにコピー
2. リモートホスト上でkubectl applyを実行
3. 各マニフェストを順次適用

### 2. Kubernetes Provider方式

TerraformのKubernetesプロバイダーを使用して直接適用します。

**設定変数**:
```hcl
use_ssh_kubectl = false
kubernetes_config_path = "/home/kigawa/.kube/config"
kubernetes_config_context = ""  # 現在のコンテキストを使用
```

**要件**:
- ローカルマシンからKubernetesクラスターへのアクセス
- 有効なkubeconfigファイル

## 設定オプション

### 共通設定

| 変数名 | デフォルト値 | 説明 |
|--------|-------------|------|
| `apply_k8s_manifests` | `true` | Kubernetesマニフェストを適用するかどうか |
| `apply_nginx_exporter` | `false` | nginx-exporterマニフェストを適用するかどうか |
| `use_ssh_kubectl` | `true` | SSH+kubectl方式を使用するかどうか |

### SSH+kubectl方式の設定

| 変数名 | デフォルト値 | 説明 |
|--------|-------------|------|
| `target_host` | `"k8s4"` | SSH接続先のホスト |
| `ssh_user` | `"kigawa"` | SSH接続用のユーザー名 |
| `ssh_key_path` | `""` | SSH秘密鍵へのパス |
| `remote_manifests_dir` | `"/tmp/k8s-manifests"` | リモートホスト上のマニフェスト配置ディレクトリ |
| `remote_kubectl_context` | `""` | kubectlコンテキスト（空の場合は現在のコンテキスト） |

### Kubernetes Provider方式の設定

| 変数名 | デフォルト値 | 説明 |
|--------|-------------|------|
| `kubernetes_config_path` | `"/home/kigawa/.kube/config"` | kubeconfigファイルのパス |
| `kubernetes_config_context` | `""` | 使用するコンテキスト |

## 使用例

### 基本的な適用

```bash
# 開発環境でマニフェストを適用
./terraform.sh plan dev
./terraform.sh apply dev

# 本番環境でマニフェストを適用
./terraform.sh plan prod
./terraform.sh apply prod
```

### nginx-exporterを有効にして適用

```bash
# nginx-exporterを含めて適用
./terraform.sh apply prod -var="apply_nginx_exporter=true"
```

### Kubernetes Provider方式で適用

```bash
# Kubernetes Provider方式を使用
./terraform.sh apply prod -var="use_ssh_kubectl=false"
```

## 前提条件

### SSH+kubectl方式の場合

1. **リモートホストへのSSHアクセス**
   ```bash
   # SSH接続テスト
   ssh -i ~/.ssh/key/id_ed25519 kigawa@192.168.1.120
   ```

2. **リモートホストでのkubectl設定**
   ```bash
   # kubectlがインストールされているか確認
   kubectl version --client
   
   # Kubernetesクラスターへのアクセス確認
   kubectl get nodes
   ```

3. **適切な権限設定**
   - kubeconfigファイルの適切な権限
   - Kubernetesリソースへの適用権限

### Kubernetes Provider方式の場合

1. **ローカルkubectl設定**
   ```bash
   # kubeconfigファイルの存在確認
   ls -la ~/.kube/config
   
   # クラスターへの接続確認
   kubectl cluster-info
   ```

2. **ファイル権限**
   ```bash
   # kubeconfigファイルの権限設定
   chmod 600 ~/.kube/config
   ```

## トラブルシューティング

### よくある問題

1. **SSH接続エラー**
   ```bash
   # SSH接続の確認
   ssh -i ~/.ssh/key/id_ed25519 -o ConnectTimeout=10 kigawa@192.168.1.120 "echo 'Connection OK'"
   ```

2. **kubectl実行エラー**
   ```bash
   # リモートホストでのkubectl確認
   ssh -i ~/.ssh/key/id_ed25519 kigawa@192.168.1.120 "kubectl version && kubectl get nodes"
   ```

3. **権限エラー**
   ```bash
   # Kubernetes権限の確認
   kubectl auth can-i create deployments
   kubectl auth can-i create services
   kubectl auth can-i create ingresses
   ```

### ログとデバッグ

```bash
# Terraformデバッグログの有効化
export TF_LOG=DEBUG
./terraform.sh apply dev

# 生成されたスクリプトの確認
cat generated/kubectl_apply.sh

# 手動でのスクリプト実行（デバッグ用）
chmod +x generated/kubectl_apply.sh
./generated/kubectl_apply.sh
```

## セキュリティ考慮事項

1. **SSH鍵の管理**
   - SSH秘密鍵をバージョン管理に含めない
   - 適切なファイル権限の設定（600）

2. **Kubernetes権限**
   - 最小権限の原則に従う
   - RBAC設定の定期的な見直し

3. **ネットワークセキュリティ**
   - IngressでのTLS設定
   - 適切なNetwork Policyの設定

4. **機密情報の管理**
   - SecretsリソースまたはExternal Secrets Operatorの使用
   - ConfigMapでの機密情報の回避