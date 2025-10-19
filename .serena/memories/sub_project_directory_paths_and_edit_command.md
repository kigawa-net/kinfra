# サブプロジェクトのディレクトリパス機能と編集コマンド

## 概要
PR #66で実装された機能：サブプロジェクトにカスタムディレクトリパスを指定する機能と、sub editコマンドの追加

## 主な変更点

### 1. SubProjectデータクラスの作成
- `model/src/main/kotlin/net/kigawa/kinfra/model/conf/SubProject.kt`
- nameとpathのプロパティを持つデータクラス
- YAMLシリアライズ時に"name:path"形式に変換

### 2. KinfraParentConfigの更新
- `subProjects`を`List<String>`から`List<SubProject>`に変更
- 後方互換性を維持するためのデシリアライズロジックを実装

### 3. サブコマンドの拡張
- `sub add <name>:<path>`形式でパス指定が可能に
- 相対パスと絶対パスの両方をサポート
- 従来の`sub add <name>`形式も継続して利用可能

### 4. sub editコマンドの追加
- `SubActionType`にEDITを追加
- `SubEditAction`クラスを実装
- エディタ連携でサブプロジェクトの設定ファイルを直接編集可能
- サンプル設定ファイルの自動生成機能

## 使用例

### サブプロジェクトの追加
```bash
# 名前のみ（従来通り）
kinfra sub add web-app

# 相対パス指定
kinfra sub add api:../api-project

# 絶対パス指定
kinfra sub add database:/opt/database
```

### サブプロジェクトの編集
```bash
# サブプロジェクトの設定ファイルをエディタで開く
kinfra sub edit web-app
```

### サブプロジェクトの詳細表示
```bash
# パス情報とフルパスを表示
kinfra sub show api
```

## 技術詳細

### パス解決
- 相対パス：親プロジェクトからの相対パスとして解決
- 絶対パス：指定されたパスをそのまま使用
- デフォルトパス：サブプロジェクト名と同じディレクトリ

### エディタ連携
- `EDITOR`環境変数またはデフォルトで`nano`を使用
- 設定ファイルが存在しない場合はサンプルを自動生成
- 非対話環境では適切なエラーメッセージを表示

### 互換性
- 既存の`kinfra-parent.yaml`ファイルは引き続き利用可能
- 新規追加時のみ新しい機能が有効になる
- YAMLフォーマットの変更なし（シリアライズ時に文字列に変換）

## テスト結果
- ✅ 名前のみでサブプロジェクト追加
- ✅ 相対パスでサブプロジェクト追加  
- ✅ 絶対パスでサブプロジェクト追加
- ✅ サブプロジェクト詳細表示（パス情報含む）
- ✅ サブプロジェクト設定ファイル編集
- ✅ 既存機能との互換性維持

## 関連ファイル
- `SubProject.kt` (新規)
- `SubEditAction.kt` (新規)
- `SubAddAction.kt` (更新)
- `SubShowAction.kt` (更新)
- `KinfraParentConfig.kt` (更新)
- `KinfraParentConfigImpl.kt` (更新)
- `ActionsModule.kt` (更新)