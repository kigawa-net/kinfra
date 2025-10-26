# 開発ガイド

## 📚 関連ドキュメント

- **[ドキュメントトップ](README.md)** - 全ドキュメントの一覧
- **[APIリファレンス](api-reference.md)** - APIとインターフェースの詳細
- **[Serenaセットアップ](serena-setup.md)** - Claude Codeでの高度なコード解析
- **[構造](structure.md)** - リポジトリの構造と組織化

## 目次

- [セットアップ](#セットアップ)
- [アーキテクチャ](#アーキテクチャ)
  - [モジュール構成](#モジュール構成)
  - [DI (Koin)](#di-koin)
- [新しいコマンドの追加](#新しいコマンドの追加)
- [Web APIエンドポイントの追加](#web-apiエンドポイントの追加)
- [テスト](#テスト)
- [コーディング規約](#コーディング規約)
- [ビルド](#ビルド)
- [デバッグ](#デバッグ)

## セットアップ

```bash
# ビルド
./gradlew build

# テスト
./gradlew test

# CLI実行
./gradlew :app-cli:run --args="help"

# Web実行
./gradlew :app-web:run
```

## アーキテクチャ

### モジュール構成

```
model → action → infrastructure → app-cli / app-web
```

- **model**: ドメインモデル（依存なし）
- **action**: ビジネスロジック契約
- **infrastructure**: 実装層
- **app-cli**: CLIアプリケーション
- **app-web**: Ktor REST API

### DI (Koin)

`app-cli/di/AppModule.kt` でコマンドを登録:

```kotlin
single(named(CommandType.INIT.commandName)) { InitCommand(get()) }
```

## 新しいコマンドの追加

### 1. CommandTypeに追加

```kotlin
// model/src/.../CommandType.kt
NEW_CMD("new-cmd")
```

### 2. Command実装

```kotlin
// app-cli/src/.../commands/NewCommand.kt
class NewCommand(private val service: SomeService) : Command {
    override fun execute(env: Environment, args: List<String>): Int {
        // 実装
    }
}
```

### 3. DI登録

```kotlin
// app-cli/di/AppModule.kt
single(named(CommandType.NEW_CMD.commandName)) { NewCommand(get()) }
```

### 4. Terraformチェックスキップ（必要時）

```kotlin
// TerraformRunner.kt
private val skipTerraformCheck = setOf(
    CommandType.HELP,
    CommandType.NEW_CMD  // 追加
)
```

## Web APIエンドポイントの追加

### 1. ルート定義

```kotlin
// app-web/src/.../routes/NewRoutes.kt
fun Route.newRoutes() {
    route("/new") {
        post { /* 実装 */ }
    }
}
```

### 2. ルーティング追加

```kotlin
// app-web/src/.../plugins/Routing.kt
routing {
    newRoutes()
}
```

## テスト

```bash
# 全テスト
./gradlew test

# 特定テスト
./gradlew test --tests "net.kigawa.kinfra.FooTest"
```

## コーディング規約

- **命名**: PascalCase (クラス), camelCase (関数・変数)
- **インデント**: スペース4つ
- **ログ**: `LoggerFactory.getLogger()`を使用
- **カラー出力**: `AnsiColors`クラスを使用

## ビルド

```bash
# Shadow JAR
./gradlew :app-cli:shadowJar
./gradlew :app-web:shadowJar

# 実行
java -jar app-cli/build/libs/kinfra-cli-all.jar
```

## デバッグ

```bash
export KINFRA_LOG_LEVEL=DEBUG
export KINFRA_LOG_DIR=/tmp/logs
```