# AGENTS

このファイルは GitHub リポジトリで使用されるエージェントの説明を保持します。

作業後はserenaを更新してください

## ビルド/テストコマンド

- **ビルド**: `./gradlew build`
- **テスト実行**: `./gradlew test`
- **単一テスト実行**: `./gradlew test --tests "TestClass.testMethod"`
- **クリーン**: `./gradlew clean`
- **JAR作成**: `./gradlew shadowJar`
- **配布物作成**: `./gradlew distTar` または `./gradlew distZip`
- **サブプロジェクト plan**: `kinfra sub plan` (各サブプロジェクトに対して terraform plan を実行)

## コードスタイルガイドライン

- **言語**: Kotlin (JVM)
- **インデント**: 4スペース
- **命名規則**:
  - クラス/インターフェース: PascalCase
  - メソッド/変数: camelCase
  - 定数: UPPER_SNAKE_CASE
- **インポート**: アルファベット順、ワイルドカードインポート禁止
- **エラーハンドリング**: Res<T, E>型を使用（Result型）
- **依存注入**: Koinを使用
- **ログ**: Loggerインターフェースを使用
- **出力**: AnsiColorsを使用した色付き出力

## ファイルパス

* ルート: /tank/var/user/dev/kigawa-net/kinfra

## ツールリスト

| ツール名   | 説明 |
|----------|------|
| **read** | 指定したファイルの内容を読み取る |
| **edit** | ファイル内の文字列を置換して保存する |
| **write** | 新規ファイルを作成または上書きする |
| **todowrite** | TODOリストを更新する |
| **todoread** | TODOリストを取得する |
| **task** | エージェントへタスクを委譲する |
| **todo-maintainer** | タスク管理を自動化するエージェント |
| **code-reviewer** | コードレビューを自動で行う |
| **greeting-responder** | ユーザーの挨拶に対して適切な返答を行う |
| **general** | 一般的なタスクを実行する |

## CI最適化

- CIワークフローを並列化: ビルド、テスト、リントを別ジョブに分割して並列実行
- 不要なクリーンステップを削除: キャッシュ活用のため
- タイムアウト調整: ビルド20分、テスト15分、リント10分
- テスト結果アップロードを失敗時のみに制限

## GitHub Actionsワークフロー

- **delete-old-merged-branches.yml**: 古いマージ済みブランチを自動削除するワークフロー。毎週日曜日または手動で実行され、30日以上前にマージされたブランチを削除。

## エージェント一覧

| エージェント名            | 説明 |
|------------------------|------|
| general            | 一般的なタスクを実行するエージェント（検索、実行、情報収集）。 |
| code-reviewer      | コードレビューを自動で行うエージェント |
| greeting-responder | ユーザーの挨拶に対して適切な返答を行う |
| todo-maintainer    | タスク管理を自動化するエージェント |

