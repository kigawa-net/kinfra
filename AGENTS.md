# AGENTS

このファイルは GitHub リポジトリで使用されるエージェントの説明を保持します。

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
- 2025-10-14: PushAction.ktをKotlinで実装し、add, commit, push機能を追加。GitHelperにaddChangesとcommitChangesメソッドを追加。
- 2025-10-14: Terraformアクションでログを表示するように変更。各アクションでquiet=falseを設定。
- 2025-10-18: GitHub Actions実行18615897430が成功。CIワークフローの高速化関連（claude.yml）。
- 2025-10-18: CIワークフローからアーティファクトアップロード処理を削除（テスト結果、CLI JAR、Web JARのアップロードを除去）。CIの高速化を図る。
- 2025-10-18: GitHub Actions実行18616218199が成功。Claudeワークフローでissueコメントによりトリガーされ、30秒で完了。
