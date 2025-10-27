# Kotlin Script設定

kinfraでは、YAMLファイルの他にKotlin Script (.kts)ファイルでも設定を定義できます。これにより、プログラム的な設定定義や型安全な設定が可能になります。

## 📚 関連ドキュメント

- **[ドキュメントトップ](README.md)** - 全ドキュメントの一覧
- **[設定リファレンス](configuration-reference.md)** - YAML設定の詳細

## 目次

- [概要](#概要)
- [kinfra-parent.kts](#kinfra-parentkts)
- [kinfra.kts](#kinfrakts)
- [使用例](#使用例)
- [利点](#利点)
- [制限事項](#制限事項)

## 概要

Kotlin Script設定では、Kotlinの構文を使用して設定を定義できます。型安全で、IDEの補完機能が利用可能です。

設定ファイルの拡張子が`.kts`の場合、自動的にKotlin Scriptとして処理されます。

## kinfra-parent.kts

親プロジェクトの設定ファイルです。

```kotlin
// kinfra-parent.kts
projectName = "my-infrastructure"
description = "Parent project for managing multiple infrastructure components"

terraform {
    version = "1.5.0"
    workingDirectory = "."

    // ネストされたbackendConfigもサポート
    backendConfig(
        "config" to mapOf(
            "bucket" to "my-terraform-state-bucket",
            "key" to "terraform.tfstate",
            "region" to "us-east-1"
        )
    )

    variableMappings(
        TerraformVariableMappingScheme(
            terraformVariable = "cloudflare_api_token",
            bitwardenSecretKey = "cloudflare-api-token"
        )
    )
}

bitwarden {
    projectId = "your-bitwarden-project-id"
}

update {
    autoUpdate = true
    checkInterval = 86400000L  // 24 hours
    githubRepo = "kigawa-net/kinfra"
}

subProjects(
    SubProjectScheme(
        projectId = "project-a",
        name = "project-a",
        description = "First sub-project",
        path = "project-a"
    ),
    SubProjectScheme(
        projectId = "project-b",
        name = "project-b",
        description = "Second sub-project",
        path = "project-b"
    )
)
```

## kinfra.kts

サブプロジェクトの設定ファイルです。

```kotlin
// kinfra.kts
project {
    projectId = "my-project"
    name = "my-project"
    description = "My infrastructure project"

    terraform {
        version = "1.5.0"
        workingDirectory = "."

        backendConfig(
            "bucket" to "my-project-terraform-state",
            "key" to "terraform.tfstate",
            "region" to "us-east-1"
        )

        variableMappings(
            TerraformVariableMappingScheme(
                terraformVariable = "api_key",
                bitwardenSecretKey = "api-key"
            ),
            TerraformVariableMappingScheme(
                terraformVariable = "secret_key",
                bitwardenSecretKey = "secret-key"
            )
        )

        outputMappings(
            TerraformOutputMappingScheme(
                terraformOutput = "instance_ip",
                bitwardenSecretKey = "instance-ip"
            )
        )

        generateOutputDir = "./generated"
    }
}

bitwarden {
    projectId = "your-project-bitwarden-id"
}

update {
    autoUpdate = true
    checkInterval = 86400000L
}

subProjects(
    SubProjectScheme(
        projectId = "sub-project-1",
        name = "sub-project-1",
        description = "Sub project 1",
        path = "sub-project-1"
    )
)
```

## 使用例

### 条件付き設定

```kotlin
// 環境に応じた設定
val isProduction = System.getenv("ENVIRONMENT") == "production"

terraform {
    version = "1.5.0"
    workingDirectory = "."

    backendConfig(
        "bucket" to if (isProduction) "prod-terraform-state" else "dev-terraform-state",
        "key" to "terraform.tfstate",
        "region" to "us-east-1"
    )
}
```

### 設定の再利用

```kotlin
// 共通設定の定義
val commonTerraformSettings = TerraformSettingsScheme(
    version = "1.5.0",
    workingDirectory = ".",
    backendConfig = mapOf(
        "bucket" to "common-terraform-state",
        "region" to "us-east-1"
    )
)

// プロジェクトごとのカスタマイズ
project {
    projectId = "project-a"
    terraform = commonTerraformSettings.copy(
        backendConfig = commonTerraformSettings.backendConfig + mapOf(
            "key" to "project-a.tfstate"
        )
    )
}
```

## 利点

1. **型安全**: Kotlinの型システムにより、設定ミスをコンパイル時に検出
2. **IDEサポート**: 補完、ナビゲーション、リファクタリングが可能
3. **プログラム的**: 条件分岐、ループ、関数呼び出しが可能
4. **再利用性**: 設定の共通化や継承が容易

## 制限事項

1. **学習コスト**: Kotlinの知識が必要
2. **実行時オーバーヘッド**: スクリプトの評価に時間がかかる場合がある
3. **デバッグ**: 実行時エラーが発生した場合のデバッグが複雑

## ファイル拡張子

- `.yaml` / `.yml`: YAML形式
- `.kts`: Kotlin Script形式

同じディレクトリに両方の形式のファイルが存在する場合、`.kts`ファイルが優先されます。