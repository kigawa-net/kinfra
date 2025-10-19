# SubShowAction Fixes

## Date
2025-10-19

## Context
PR #59のSubShowAction実装に対する修正。@kigawa01さんから「修正して」のコメントがあり、エラーハンドリングと出力形式を改善。

## Issues Fixed

### 1. エラーハンドリングの一貫性
- **Problem**: 設定ファイルが存在しない場合にErrorとして扱い、return 1としていた
- **Solution**: Warningとして扱い、return 0に変更（SubListActionと統一）
- **Impact**: ユーザー体験の向上、一貫性のあるエラーハンドリング

### 2. 出力形式の統一
- **Problem**: 「Parent config」という表記が他のアクションと異なっていた
- **Solution**: 「Config file」に変更してSubListActionと統一
- **Impact**: 一貫性のあるUI/UX

### 3. エラーメッセージの改善
- **Problem**: 設定ファイルが見つからない場合の情報が不足していた
- **Solution**: 探しているファイル名を明記するNoteを追加
- **Impact**: ユーザーが問題を特定しやすくなる

## Testing
- ビルド成功: `./gradlew build`
- テスト成功: `./gradlew test`
- コマンド実行確認: `./gradlew :app-cli:run --args="sub show test-project"`

## Result
PR #63としてマージ待ち。エラーハンドリングの一貫性が向上し、ユーザー体験が改善された。