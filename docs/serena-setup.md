# Serenaセットアップガイド

## 概要

Serenaは、Claude Codeに高度なコード解析機能を提供するMCP（Model Context Protocol）サーバーです。
言語サーバープロトコル（LSP）を活用し、シンボルレベルのコード検索・編集を可能にします。

## 📚 関連ドキュメント

- **[ドキュメントトップ](README.md)** - 全ドキュメントの一覧
- **[開発ガイド](development-guide.md)** - 開発環境とベストプラクティス
- **[APIリファレンス](api-reference.md)** - APIとインターフェースの詳細
- **[構造](structure.md)** - リポジトリの構造と組織化

## 主な機能

- **セマンティックコード検索**: シンボル定義、参照、実装の検索
- **コード編集**: シンボルレベルでの正確な編集
- **広範な言語サポート**: Python、TypeScript/JavaScript、Kotlin、Java、Go、Rust等 25以上の言語
- **プロジェクトメモリ**: 複数セッションにわたるコンテキスト保持

## 前提条件

### 必須

- **uv**: Python環境管理ツール
  - インストール: https://docs.astral.sh/uv/getting-started/installation/

### 推奨

- クリーンなGit状態から作業
- テストとロギングの整備

## セットアップ手順

### 1. uvのインストール

まだインストールしていない場合は、公式ドキュメントに従ってuvをインストールしてください。

Linux/macOS:
```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

### 2. Claude CodeへのMCPサーバー追加

プロジェクトルートディレクトリで以下のコマンドを実行します：

```bash
claude mcp add serena -- uvx --from git+https://github.com/oraios/serena serena start-mcp-server --context ide-assistant --project "$(pwd)"
```

**パラメータ説明**：
- `--context ide-assistant`: Claude Code向けの最適化されたコンテキスト
- `--project "$(pwd)"`: 現在のディレクトリをプロジェクトとして設定

### 3. Claude Codeの再起動

MCPサーバー設定を反映するため、Claude Codeを再起動します。

### 4. 動作確認

Claude Codeで以下のような操作を試してください：

```
「TerraformServiceインターフェースの実装を全て検索」
「CommandInterpreterクラスの参照箇所を表示」
```

## 設定オプション

### プロジェクト設定（オプション）

プロジェクトルートに `.serena/project.yml` を作成することで、プロジェクト固有の設定が可能です：

```yaml
# 例: 特定のディレクトリを除外
exclude_patterns:
  - "**/build/**"
  - "**/node_modules/**"
  - "**/.gradle/**"
```

### ユーザー設定（オプション）

`~/.serena/serena_config.yml` でユーザーレベルの設定が可能です。

## Kotlinプロジェクト固有の注意点

### Kotlin言語サーバー

SerenaはKotlinをサポートしていますが、最適な動作のためにKotlin言語サーバーが必要な場合があります。

### Gradle プロジェクト

- ビルド成果物 (`build/` ディレクトリ) を除外することを推奨
- `.serena/project.yml` で除外パターンを設定

## トラブルシューティング

### MCPサーバーが起動しない

1. uvが正しくインストールされているか確認：
   ```bash
   uv --version
   ```

2. 手動でSerenaを起動して詳細なエラーを確認：
   ```bash
   uvx --from git+https://github.com/oraios/serena serena start-mcp-server --context ide-assistant --project "$(pwd)"
   ```

### シンボル検索が機能しない

1. プロジェクトがインデックスされているか確認
2. 対象言語の言語サーバーが利用可能か確認
3. Gradleビルドが成功しているか確認

### パフォーマンスの問題

- `.serena/project.yml` で不要なディレクトリを除外
- 大規模なビルド成果物やキャッシュディレクトリを除外

## ダッシュボード

Serenaは管理用ダッシュボードを提供します：

- URL: http://localhost:24282/dashboard/index.html
- 機能: ログ監視、サーバー状態確認

## 参考リンク

- Serena公式リポジトリ: https://github.com/oraios/serena
- MCP仕様: https://modelcontextprotocol.io/
- uv公式ドキュメント: https://docs.astral.sh/uv/

## KInfraプロジェクトでの活用例

### コード探索

```
「DependencyContainerクラスの全ての使用箇所を検索」
「ActionTypeに新しい値を追加した際の影響範囲を調査」
```

### リファクタリング

```
「ConfigRepositoryの実装をリファクタリング」
「CommandInterpreterの依存関係を整理」
```

### バグ修正

```
「CommandInterpreterのエラーハンドリングを改善」
「TerraformServiceの例外処理を追加」
```
