# kinfra sub rmコマンドの実装

## 概要
`kinfra sub rm <name>`コマンドを追加して、サブプロジェクトを削除する機能を実装。

## 実装内容

### 1. SubActionTypeの拡張
- `REMOVE("rm")`をenumに追加
- コマンドラインでの短縮形`rm`に対応

### 2. SubRemoveActionクラスの実装
**ファイル**: `action/src/main/kotlin/net/kigawa/kinfra/action/actions/SubRemoveAction.kt`

**主な機能**:
- 引数チェック（サブプロジェクト名必須）
- 親設定ファイルの存在確認
- サブプロジェクトの存在確認
- 削除処理の実行
- エラーハンドリングとユーザーフレンドリーなメッセージ

**エラーケース**:
- 引数なし：使用方法を表示
- 親設定ファイルなし：エラーメッセージと作成案内
- サブプロジェクト不在：利用可能なプロジェクト一覧を表示

### 3. DIモジュールへの登録
**ファイル**: `app-cli/src/main/kotlin/net/kigawa/kinfra/di/ActionsModule.kt`
- `SubRemoveAction`を`sub rm`コマンドとして登録

## 使用方法

### 基本構文
```bash
kinfra sub rm <project-name>
```

### 使用例
```bash
# サブプロジェクトを削除
kinfra sub rm test-project

# 存在しないプロジェクトを削除（エラー表示）
kinfra sub rm nonexistent-project

# 引数なし（エラー表示）
kinfra sub rm
```

## 実行結果

### 成功時
```
Removed: Sub-project 'test-project:/tmp/test-project'
Total sub-projects: 4
```

### エラー時
```
Error: Sub-project 'nonexistent-project' not found
Available sub-projects:
  - k8s
  - web-app
  - api:/opt/api-project
  - database:../database
```

## テスト結果

### 正常系テスト
- ✅ サブプロジェクトの正常な削除
- ✅ 削除後のリスト更新
- ✅ YAMLファイルのオブジェクト形式維持
- ✅ 合計数の正確な表示

### 異常系テスト
- ✅ 引数なしの場合のエラー表示
- ✅ 存在しないプロジェクト名の場合のエラー表示
- ✅ 利用可能なプロジェクト一覧の表示
- ✅ 親設定ファイル不在の場合のエラー表示

### ヘルプ統合
- ✅ `--help`でのコマンド表示
- ✅ 説明文の適切な表示

## 技術仕様

### 削除ロジック
1. 引数バリデーション
2. 親設定ファイルの読み込み
3. サブプロジェクトの存在確認
4. リストからのフィルタリング（name != 削除対象名）
5. 設定ファイルの保存
6. 結果の表示

### YAML形式の維持
- 削除後もオブジェクト形式を維持
- 既存のサブプロジェクトの形式を変更しない
- 後方互換性を維持

### エラーハンドリング
- 例外キャッチと適切なエラーメッセージ
- ユーザーが次にとるべきアクションの提案
- カラー付き出力での視覚的な分かりやすさ

## コード品質
- ✅ コンパイル成功
- ✅ テストパス
- ✅ 既存コードとの整合性
- ✅ エラーハンドリングの網羅性
- ✅ ユーザーエクスペリエンスの配慮

## 今後の拡張可能性
- 確認プロンプトの追加（`--force`オプション）
- 複数サブプロジェクトの一括削除
- ワイルドカードパターンでの削除
- 削除確認のインタラクティブモード