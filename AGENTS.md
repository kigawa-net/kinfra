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

## エージェント一覧

| エージェント名            | 説明 |
|------------------------|------|
| general            | 一般的なタスクを実行するエージェント（検索、実行、情報収集）。 |
| code-reviewer      | コードレビューを自動で行うエージェント |
| greeting-responder | ユーザーの挨拶に対して適切な返答を行う |
| todo-maintainer    | タスク管理を自動化するエージェント |

## 変更履歴
- 2025-10-26: NextActionを修正。`kinfra next` コマンドでローカル変更がある場合に自動stashしてpullし、変更があったサブプロジェクトのみを有効化。stash復元時にコンフリクトが発生した場合でも処理を続行。divergent branchesがある場合に--no-rebaseオプションでpullする機能を追加。
- 2025-10-26: PR #164 を作成。generateOutputDir設定のドキュメントを追加。
- 2025-10-26: PR #159 を更新。Claudeワークフローをcreate-pull-requestアクションで簡素化。
- 2025-10-26: PR #159 を作成。Claudeワークフロー修正とプロジェクトドキュメント更新を実装。
- 2025-10-26: ClaudeワークフローのGit操作エラーを修正。Create Pull Request Or Commentステップでのexit code 128エラーを解決。
- 2025-10-26: PR #153 を作成。Terraformのバックエンド設定サポートとデプロイパイプラインの改善を実装。
- 2025-10-25: kinfra deploy コマンド実行時に "Parent project deployment failed" エラーが発生。原因は kinfra.yaml が存在せず、Terraform 設定がないため。kinfra はインフラ管理ツールであり、自分自身のデプロイには使用できない。
- 2025-10-25: kinfra.yamlおよびkinfra-parent.yamlのbackendConfig設定を読み込み、Terraformコマンドに-backend-configオプションとして渡す機能を追加。Terraformのバックエンド設定を自動適用。
- 2025-10-25: `kinfra current generate variable` コマンドに--output-dirオプションを追加。variables.tfの出力ディレクトリを指定できるようにした。
- 2025-10-25: `kinfra current generate variable` コマンドを拡張。kinfra.yamlまたはkinfra-parent.yamlのvariableMappingsから全ての変数を生成できるようにした。引数なしで実行すると全ての変数を生成、引数ありで特定の変数を生成。
- 2025-10-24: plan実行時に自動でterraform initを実行する機能を追加。PlanActionとSubPlanActionでplan前にinitを実行するように変更。
- 2025-10-23: tfvars生成機能を削除。DeployActionWithSDK.kt、DeployAction.kt、DeploymentPipeline.ktからbackend setupコードを削除し、Terraformワークフローを簡素化。
- 2025-10-22: .bws_tokenファイルを~/.local/kinfra/.bws_tokenに配置するように修正。
- 2025-10-22: GlobalConfigCompleterインターフェースを実装し、設定補完機能を追加。
