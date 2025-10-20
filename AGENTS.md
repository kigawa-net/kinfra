# AGENTS

このファイルは GitHub リポジトリで使用されるエージェントの説明を保持します。

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

## エージェント一覧

| エージェント名            | 説明 |
|------------------------|------|
| general            | 一般的なタスクを実行するエージェント（検索、実行、情報収集）。 |
| code-reviewer      | コードレビューを自動で行うエージェント |
| greeting-responder | ユーザーの挨拶に対して適切な返答を行う |
| todo-maintainer    | タスク管理を自動化するエージェント |

## 変更履歴
- 2025-10-21: GitHub Actionsワークフローの構文エラーを修正。TARGET_NUM変数の代入方法を改善し、git diff HEAD~1の安全なチェックを追加。
- 2025-10-21: planコマンド実行時に deprecated Gradle フィーチャー警告を表示するようにし、`--warning-mode all` の使用を推奨。
- 2025-10-20: kinfra planで全てのプロジェクトでplanを実行する。親プロジェクトとサブプロジェクトの両方で terraform plan を実行。
- 2025-10-20: kinfra sub planコマンドを追加。サブプロジェクトで terraform plan を実行できるようにする。
- 2025-10-20: サブプロジェクト実行時にkinfra-parent.yamlがない場合にメッセージを表示。
- 2025-10-20: PlanActionでTerraform設定がない場合にスキップする。
- 2025-10-20: PlanActionでTerraform設定がない場合にエラーを出すように戻す。
- 2025-10-20: サブプロジェクトのTerraform実行がスキップされた場合に成功として扱う。
- 2025-10-20: plan実行前にプロジェクト名を表示する。
- 2025-10-20: Terraform実行がスキップされた場合に成功として扱う。
- 2025-10-20: GitHub Actionsワークフローで無効な条件式を修正。
- 2025-10-20: Terraform設定がnullの場合にエラーを出さずにスキップする。
- 2025-10-20: Terraformアクションのエラーメッセージを詳細に表示。
- 2025-10-20: GitHub Actionsワークフローでcreate-pull-requestアクションを使用。
- 2025-10-20: YAMLデシリアライズエラーと入力ストリーム競合を修正。
- 2025-10-19: PR #95 をマージ。
- 2025-10-19: PR #66 を作成。
- 2025-10-19: PR #65 をマージ。
- 2025-10-19: PR #64 をマージ。
- 2025-10-19: PR #56 を作成。
- 2025-10-19: kinfra sub addコマンドを追加。
- 2025-10-19: CIワークフローを高速化。
- 2025-10-18: AppModuleを細分化。
- 2025-10-18: サブコマンドの構造を改善。
- 2025-10-18: sub listコマンドを追加。
- 2025-10-14: PushAction.ktを実装。GitHelperにaddChangesとcommitChangesメソッドを追加。
- 2025-10-14: Terraformアクションでログを表示。
- 2025-10-14: setup-r2コマンドを削除。
- 2025-10-14: config-editコマンドを追加。
- 2025-10-19: TerraformRunnerクラスのリファクタリングを実施。