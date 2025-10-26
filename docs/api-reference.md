# API リファレンス

このドキュメントでは、kinfra Web API の詳細なリファレンスを提供します。

## 📚 関連ドキュメント

- **[ドキュメントトップ](README.md)** - 全ドキュメントの一覧
- **[開発ガイド](development-guide.md)** - 開発環境とベストプラクティス
- **[コマンドリファレンス](command-reference.md)** - CLIコマンドの詳細
- **[設定リファレンス](configuration-reference.md)** - 設定オプションの詳細

## 目次

- [概要](#概要)
- [サーバー起動](#サーバー起動)
  - [Gradleから起動](#gradleから起動)
  - [JAR から起動](#jar-から起動)
- [認証](#認証)
- [エンドポイント](#エンドポイント)
  - [ヘルスチェック](#ヘルスチェック)
    - [GET /](#get-)
    - [GET /health](#get-health)
  - [Terraform Init](#terraform-init)
  - [Terraform Plan](#terraform-plan)
  - [Terraform Apply](#terraform-apply)
  - [Terraform Destroy](#terraform-destroy)
  - [Terraform Validate](#terraform-validate)
  - [Terraform Format](#terraform-format)
- [エラーレスポンス](#エラーレスポンス)
  - [エラーレスポンス形式](#エラーレスポンス形式)
  - [一般的なエラー](#一般的なエラー)
- [CORS設定](#cors設定)
- [レート制限](#レート制限)
- [データ型](#データ型)
  - [TerraformRequest](#terraformrequest)
  - [TerraformResponse](#terraformresponse)
- [セキュリティ考慮事項](#セキュリティ考慮事項)
- [使用例](#使用例)
  - [Python](#python)
  - [JavaScript (Node.js)](#javascript-nodejs)
  - [cURL スクリプト例](#curl-スクリプト例)
- [トラブルシューティング](#トラブルシューティング)
  - [サーバーが起動しない](#サーバーが起動しない)
  - [JSONパースエラー](#jsonパースエラー)
  - [タイムアウト](#タイムアウト)
- [関連ドキュメント](#関連ドキュメント)

## 概要

kinfra Web APIは、Ktor フレームワークを使用して構築されたRESTful APIです。TerraformコマンドをHTTP経由で実行できます。

- **ベースURL**: `http://localhost:8080`
- **コンテンツタイプ**: `application/json`
- **デフォルトポート**: 8080

## サーバー起動

### Gradleから起動

```bash
./gradlew :app-web:run
```

### JAR から起動

```bash
# Shadow JARをビルド
./gradlew :app-web:shadowJar

# JAR を実行
java -jar app-web/build/libs/kinfra-web-all.jar
```

## 認証

現在のバージョンでは認証は実装されていません。ローカルネットワークまたは信頼できる環境での使用を想定しています。

## エンドポイント

### ヘルスチェック

#### GET /

APIの稼働状態を確認します。

**レスポンス**

```json
{
  "status": "OK",
  "message": "kinfra API is running"
}
```

**ステータスコード**
- `200 OK`: APIは正常に動作しています

---

#### GET /health

ヘルスチェックエンドポイント。

**レスポンス**

```json
{
  "status": "healthy"
}
```

**ステータスコード**
- `200 OK`: サービスは正常です

---

### Terraform Init

#### POST /terraform/init

Terraform の初期化を実行します。

**リクエストボディ**

```json
{
  "command": "init"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| command | string | Yes | コマンド名 ("init") |

**レスポンス**

```json
{
  "success": true,
  "message": "Init successful",
  "output": "Terraform has been successfully initialized!...",
  "exitCode": 0
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| success | boolean | 実行が成功したかどうか |
| message | string | 実行結果のメッセージ |
| output | string | コマンドの標準出力 (nullable) |
| exitCode | integer | 終了コード (nullable) |

**ステータスコード**
- `200 OK`: リクエストが処理されました
- `500 Internal Server Error`: サーバーエラーが発生

**使用例**

```bash
curl -X POST http://localhost:8080/terraform/init \
  -H "Content-Type: application/json" \
  -d '{"command": "init"}'
```

---

### Terraform Plan

#### POST /terraform/plan

Terraform の実行計画を作成します。

**リクエストボディ**

```json
{
  "command": "plan"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| command | string | Yes | コマンド名 ("plan") |

**レスポンス**

```json
{
  "success": true,
  "message": "Plan successful",
  "output": "Terraform will perform the following actions:...",
  "exitCode": 0
}
```

**ステータスコード**
- `200 OK`: リクエストが処理されました
- `500 Internal Server Error`: サーバーエラーが発生

**使用例**

```bash
curl -X POST http://localhost:8080/terraform/plan \
  -H "Content-Type: application/json" \
  -d '{"command": "plan"}'
```

---

### Terraform Apply

#### POST /terraform/apply

Terraform の変更を適用します。

**リクエストボディ**

```json
{
  "command": "apply"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| command | string | Yes | コマンド名 ("apply") |

**レスポンス**

```json
{
  "success": true,
  "message": "Apply successful",
  "output": "Apply complete! Resources: 5 added, 2 changed, 1 destroyed.",
  "exitCode": 0
}
```

**ステータスコード**
- `200 OK`: リクエストが処理されました
- `500 Internal Server Error`: サーバーエラーが発生

**使用例**

```bash
curl -X POST http://localhost:8080/terraform/apply \
  -H "Content-Type: application/json" \
  -d '{"command": "apply"}'
```

**注意**: このコマンドは `-auto-approve` フラグで実行されるため、確認なしで変更が適用されます。

---

### Terraform Destroy

#### POST /terraform/destroy

Terraform で管理されているリソースを削除します。

**リクエストボディ**

```json
{
  "command": "destroy"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| command | string | Yes | コマンド名 ("destroy") |

**レスポンス**

```json
{
  "success": true,
  "message": "Destroy successful",
  "output": "Destroy complete! Resources: 8 destroyed.",
  "exitCode": 0
}
```

**ステータスコード**
- `200 OK`: リクエストが処理されました
- `500 Internal Server Error`: サーバーエラーが発生

**使用例**

```bash
curl -X POST http://localhost:8080/terraform/destroy \
  -H "Content-Type: application/json" \
  -d '{"command": "destroy"}'
```

**警告**: このコマンドはリソースを完全に削除します。本番環境では慎重に使用してください。

---

### Terraform Validate

#### POST /terraform/validate

Terraform 設定ファイルの構文を検証します。

**リクエストボディ**

```json
{
  "command": "validate"
}
```

**レスポンス**

```json
{
  "success": true,
  "message": "Validation successful",
  "output": "Success! The configuration is valid.",
  "exitCode": 0
}
```

**ステータスコード**
- `200 OK`: リクエストが処理されました
- `500 Internal Server Error`: サーバーエラーが発生

**使用例**

```bash
curl -X POST http://localhost:8080/terraform/validate \
  -H "Content-Type: application/json" \
  -d '{"command": "validate"}'
```

---

### Terraform Format

#### POST /terraform/format

Terraform 設定ファイルを標準形式にフォーマットします。

**リクエストボディ**

```json
{
  "command": "format"
}
```

**レスポンス**

```json
{
  "success": true,
  "message": "Format successful",
  "output": "main.tf\nvariables.tf",
  "exitCode": 0
}
```

**ステータスコード**
- `200 OK`: リクエストが処理されました
- `500 Internal Server Error`: サーバーエラーが発生

**使用例**

```bash
curl -X POST http://localhost:8080/terraform/format \
  -H "Content-Type: application/json" \
  -d '{"command": "format"}'
```

---

## エラーレスポンス

すべてのエンドポイントは、エラー時に以下の形式でレスポンスを返します。

### エラーレスポンス形式

```json
{
  "success": false,
  "message": "Error: <エラーメッセージ>",
  "output": null,
  "exitCode": null
}
```

### 一般的なエラー

#### 400 Bad Request

リクエストの形式が不正です。

```json
{
  "success": false,
  "message": "Error: Invalid request format"
}
```

#### 500 Internal Server Error

サーバー内部でエラーが発生しました。

```json
{
  "success": false,
  "message": "Error: Terraform execution failed"
}
```

---

## CORS設定

APIはCORS (Cross-Origin Resource Sharing) をサポートしています。

- **許可されたメソッド**: GET, POST, PUT, DELETE, OPTIONS
- **許可されたヘッダー**: Content-Type, Authorization
- **許可されたオリジン**: すべて (開発環境用)

本番環境では、適切なオリジン制限を設定してください。

---

## レート制限

現在のバージョンではレート制限は実装されていません。

---

## データ型

### TerraformRequest

```kotlin
data class TerraformRequest(
    val command: String
)
```

### TerraformResponse

```kotlin
data class TerraformResponse(
    val success: Boolean,
    val message: String,
    val output: String? = null,
    val exitCode: Int? = null
)
```

---

## セキュリティ考慮事項

1. **認証なし**: 現在のバージョンでは認証が実装されていません。信頼できるネットワーク内でのみ使用してください。

2. **自動承認**: `apply` と `destroy` コマンドは自動的に承認されます。本番環境では追加の確認メカニズムを検討してください。

3. **ログ出力**: コマンドの出力がレスポンスに含まれます。機密情報が含まれないように注意してください。

4. **CORS**: 開発環境ではすべてのオリジンが許可されています。本番環境では制限してください。

---

## 使用例

### Python

```python
import requests
import json

# API ベースURL
base_url = "http://localhost:8080"

# Terraform Init
response = requests.post(
    f"{base_url}/terraform/init",
    json={"command": "init"},
    headers={"Content-Type": "application/json"}
)

result = response.json()
print(f"Success: {result['success']}")
print(f"Message: {result['message']}")
```

### JavaScript (Node.js)

```javascript
const fetch = require('node-fetch');

async function runTerraformInit() {
  const response = await fetch('http://localhost:8080/terraform/init', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      command: 'init'
    })
  });

  const result = await response.json();
  console.log(`Success: ${result.success}`);
  console.log(`Message: ${result.message}`);
}

runTerraformInit();
```

### cURL スクリプト例

```bash
#!/bin/bash

# 変数設定
API_URL="http://localhost:8080"

# Init
echo "Running init..."
curl -X POST "$API_URL/terraform/init" \
  -H "Content-Type: application/json" \
  -d '{"command": "init"}'

# Plan
echo "Running plan..."
curl -X POST "$API_URL/terraform/plan" \
  -H "Content-Type: application/json" \
  -d '{"command": "plan"}'

# Apply
echo "Running apply..."
curl -X POST "$API_URL/terraform/apply" \
  -H "Content-Type: application/json" \
  -d '{"command": "apply"}'
```

---

## トラブルシューティング

### サーバーが起動しない

```bash
# ポートが使用中か確認
lsof -i :8080

# 別のポートを使用
export PORT=8081
./gradlew :app-web:run
```

### JSONパースエラー

リクエストボディが正しいJSON形式であることを確認してください。

```bash
# 正しい形式
curl -X POST http://localhost:8080/terraform/init \
  -H "Content-Type: application/json" \
  -d '{"command": "init"}'

# 間違った形式（シングルクォート）
curl -X POST http://localhost:8080/terraform/init \
  -H "Content-Type: application/json" \
  -d "{'command': 'init'}"  # NG
```

### タイムアウト

長時間実行されるコマンド（特に `apply` や `destroy`）は、タイムアウトが発生する可能性があります。Terraformの設定やネットワーク環境を確認してください。

---

## 関連ドキュメント

- [開発ガイド](./development-guide.md)
- [コマンドリファレンス](./command-reference.md)
- [設定リファレンス](./configuration-reference.md)