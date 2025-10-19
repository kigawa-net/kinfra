# コンフリクト解消 - 2025-10-19

## 問題
PR #63でdevブランチとのコンフリクトが発生

## 原因
1. ActionsModule.ktでコンフリクトマーカーが残っていた
2. Actionインターフェースのシグネチャ変更（Array<String> → List<String>）
3. 各アクションのコンストラクタシグネチャ変更

## 解消手順
1. `git pull origin dev --rebase`でリベース実行
2. ActionsModule.ktのコンフリクトを手動解消
3. SubShowActionのexecuteメソッドシグネチャを修正
4. 各アクションのコンストラクタ引数を修正
5. ビルドとテストで確認

## 修正内容
- SubShowAction.execute(args: Array<String>) → execute(args: List<String>)
- ConfigEditAction: ConfigEditAction(get(), get(), get()) → ConfigEditAction(get(), get())
- SubListAction: SubListAction(get(), get()) → SubListAction(get())
- SubAddAction: SubAddAction(get(), get()) → SubAddAction(get())
- SelfUpdateAction: 引数を1つ削除

## 結果
- ✅ ビルド成功
- ✅ テスト成功
- ✅ SubShowActionが正しく動作
- PR #63が更新され、マージ待ち