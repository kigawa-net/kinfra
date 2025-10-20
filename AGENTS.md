# AGENTS

このファイルは GitHub リポジトリで使用されるエージェントの説明を保持します。

## ビルド/テストコマンド

- **ビルド**: `./gradlew build`
- **テスト実行**: `./gradlew test`
- **単一テスト実行**: `./gradlew test --tests "TestClass.testMethod"`
- **クリーン**: `./gradlew clean`
- **JAR作成**: `./gradlew shadowJar`
- **配布物作成**: `./gradlew distTar` または `./gradlew distZip`

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
| **todowrite** | TODOリストを更新する |
| **todoread** | TODOリストを読む |
| **task** | エージェントで複雑タスクを実行する |
| **todo-maintainer** | タスク管理を自動化するエージェント |
| **code-reviewer** | コードレビューを自動で行う |
| **greeting-responder** | 挨拶に対する返答をする |
| **general** | 一般的なタスクを実行する |


## エージェント一覧

| エージェント名            | 説明                              |
|--------------------|---------------------------------|
| general            | 一般的なタスクを実行するエージェント（検索、実行、情報収集）。 |
| code-reviewer      | コードレビューを自動で行うエージェント             |
| greeting-responder | ユーザーの挨拶に対して適切な返答を行うエージェント       |
| todo-maintainer    | タスク管理を自動化するエージェント |

## 変更履歴
- 2025-10-20: YAMLデシリアライズエラーと入力ストリーム競合を修正。ProjectInfoSchemeに@SerialName("projectId")を追加、LoginActionでreadlnOrNull()を使用、bin/commonにgit merge origin/devを追加。ConfigRepositoryImplでstrictMode=falseを設定してUnknown propertyを無視。Terraformエラー時にプロジェクト情報を表示する機能を追加。Terraform設定がnullの場合に実行しない機能を追加。
- 2025-10-19: PR #95 をマージ。ProjectInfoSchemeのシリアライズフィールド名を修正してprojectIdプロパティを正しく読み込み。
- 2025-10-19: PR #66 を作成。サブプロジェクトにディレクトリ指定機能を追加。SubProjectデータクラスを作成し、name:path形式でパス指定を可能に。sub editコマンドも追加。
- 2025-10-19: PR #65 をマージ。config -p editコマンドの解析を修正。CommandInterpreterがフラグをスキップしてサブコマンドを正しく検出するように改善。
- 2025-10-19: PR #64 をマージ。コンパイルエラーを修正。ConfigRepositoryImpl、GlobalConfig、ActionsModuleの型不一致とパラメータエラーを解決。
- 2025-10-19: PR #56 を作成。kigawaブランチからdevブランチに向けて、kinfra sub addコマンドの実装。タイトルと本文を日本語に変更。
- 2025-10-19: kinfra sub addコマンドを追加。SubActionTypeにADDを追加、SubAddActionを実装、ActionsModuleに登録、ドキュメント更新。kinfra-parent.yamlにサブプロジェクトを追加できるようにする。
- 2025-10-19: CIワークフローを高速化。testとlintジョブを統合、Gradle並列実行・ビルドキャッシュ有効化、条件付き実行を追加。
- 2025-10-19: CIワークフローでgradle/actionsの無効なパラメータbuild-cacheを削除して警告を解決。
- 2025-10-19: CIテスト失敗を修正。ActionTypeにSUBを追加し、AppModule.ktとActionsModule.ktのimportを修正してコンパイルエラーを解決。
- 2025-10-19: ClaudeワークフローのPR編集を上書きから追記に変更。既存PR本文を取得して追記するように修正。
- 2025-10-18: AppModuleを細分化。InfrastructureModule.kt, BitwardenModule.kt, ActionsModule.ktを作成し、AppModule.ktをリファクタリングしてモジュールを組み合わせる。
- 2025-10-18: サブコマンドの構造を改善。SubActionType enumを作成し、ActionTypeからSUB_LISTを削除してSUBを追加。TerraformRunnerとAppModuleでサブコマンドの解析と登録を更新。
- 2025-10-18: sub listコマンドを追加。SubListAction.ktを実装し、kinfra-parent.yamlからサブプロジェクト一覧を表示。ActionType.kt, AppModule.kt, ドキュメントに追加。
- 2025-10-14: PushAction.ktをKotlinで実装し、add, commit, push機能を追加。GitHelperにaddChangesとcommitChangesメソッドを追加。
- 2025-10-14: Terraformアクションでログを表示するように変更。各アクションでquiet=falseを設定。
- 2025-10-18: GitHub Actions実行18615897430が成功。CIワークフローの高速化関連（claude.yml）。
- 2025-10-18: CIワークフローからアーティファクトアップロード処理を削除（テスト結果、CLI JAR、Web JARのアップロードを除去）。CIの高速化を図る。
- 2025-10-18: GitHub Actions実行18616218199が成功。Claudeワークフローでissueコメントによりトリガーされ、30秒で完了。
- 2025-10-14: setup-r2コマンドを削除。関連ファイル（SetupR2Action.kt, SetupR2ActionWithSDK.kt）を削除し、ActionType.kt, AppModule.kt, TerraformRunner.kt, DeployAction.kt, ドキュメントから参照を削除。
- 2025-10-14: config-editコマンドを追加。ConfigEditAction.ktを実装し、ActionType.kt, AppModule.kt, ドキュメントに追加。
- 2025-10-14: setup-r2コマンドの残存参照を完全に削除。TerraformRunner.kt, DeployAction.kt, ドキュメントから参照を削除。
- 2025-10-14: origin/devをmainにマージ。プルリクエストを作成。
- 2025-10-19: TerraformRunnerクラスのリファクタリングを実施。単一責任の原則に基づき、以下のコンポーネントに分割：
  - ActionRegistry: アクションの登録と管理を担当
  - CommandInterpreter: コマンドライン引数の解釈を担当
  - SystemRequirement: システム要件のチェック（Terraformの存在確認）を担当
  - UpdateHandler: アップデートのチェックと実行を担当
  - 命名規則の改善：ServiceやManagerといった一般的すぎる名前を避け、具体的な責務を表す名前に変更
  - TerraformRunnerは各コンポーネントを統合する役割に特化し、コードの可読性と保守性を向上
