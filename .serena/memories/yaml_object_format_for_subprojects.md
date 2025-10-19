# サブプロジェクトのYAMLオブジェクト形式化

## 概要
サブプロジェクトリストを文字列形式からオブジェクト形式に変更し、YAMLの可読性と構造化を向上。

## 変更内容

### 変更前（文字列形式）
```yaml
subProjects:
- "k8s"
- "web-app"
- "api:/opt/api-project"
- "database:../database"
```

### 変更後（オブジェクト形式）
```yaml
subProjects:
- name: "k8s"
  path: "k8s"
- name: "web-app"
  path: "web-app"
- name: "api"
  path: "/opt/api-project"
- name: "database"
  path: "../database"
```

## 技術実装

### 1. 新規ファイル
- `SubProjectScheme.kt` - SubProjectのシリアライズ用データクラス

### 2. 更新ファイル
- `SubProject.kt` - toString()メソッドを削除（オブジェクトシリアライズ用）
- `KinfraParentConfigScheme.kt` - subProjectsをList<SubProjectScheme>に変更
- `KinfraParentConfigImpl.kt` - 後方互換性ロジックを実装
- `LoginRepoImpl.kt` - loadKinfraParentConfig()を更新
- `ConfigRepositoryImpl.kt` - loadKinfraParentConfig()を更新
- `KinfraConfigScheme.kt` - 各設定クラスに変換メソッドを追加

### 3. 後方互換性
- `KinfraParentConfigImpl.fromFile()`メソッドで両形式をサポート
- まず新しいオブジェクト形式でのデコードを試行
- 失敗した場合はレガシーな文字列形式としてデコード
- `LegacyKinfraParentConfigScheme`で古い形式を処理

## 利点

### 1. 可読性向上
- 各サブプロジェクトのプロパティが明確に表示
- YAMLエディタでのシンタックスハイライトが改善
- 構造が直感的に理解しやすい

### 2. 拡張性
- 将来的に新しいプロパティを追加しやすい（例: description, type, tags）
- YAMLコメントを各プロパティに追加可能
- バリデーションが容易になる

### 3. 互換性維持
- 既存の文字列形式のYAMLファイルを引き続き読み込み可能
- 自動的に新しいオブジェクト形式に変換して保存
- ユーザーは移行を意識する必要なし

## テスト結果

### 後方互換性テスト
- ✅ 古い文字列形式のYAMLを正しく読み込み
- ✅ SubProjectオブジェクトに正しく変換
- ✅ 全てのsubコマンドが正常に動作

### 新形式テスト
- ✅ 新しいサブプロジェクトをオブジェクト形式で保存
- ✅ 読み込み・表示・編集機能が正常に動作
- ✅ 既存機能との互換性を維持

## 使用例

### コマンド実行
```bash
# サブプロジェクト一覧表示
kinfra sub list

# サブプロジェクト追加（自動的にオブジェクト形式で保存）
kinfra sub add new-project:/tmp/new-project

# サブプロジェクト詳細表示
kinfra sub show api
```

### 出力例
```
=== Sub-projects in kigawa-infra ===

  1. SubProject(name=k8s, path=k8s)
  2. SubProject(name=web-app, path=web-app)
  3. SubProject(name=api, path=/opt/api-project)
  4. SubProject(name=database, path=../database)

Total: 4 sub-project(s)
Config file: /home/kigawa/.local/kinfra/repos/infra/kinfra-parent.yaml
```

## 今後の拡張可能性
- descriptionプロパティの追加
- typeプロパティ（例: terraform, ansible, docker）の追加
- enabledフラグの追加
- tagsやmetadataの追加
- 環境ごとの設定オーバーライド機能