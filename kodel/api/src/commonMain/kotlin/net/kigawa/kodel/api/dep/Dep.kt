package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

/**
 * 依存を表すクラス。
 * 依存の作成と取得を管理する。
 *
 * @param T 依存の型
 * @param S 依存スコープの型
 * @param depProviderFactory 依存プロバイダファクトリ
 * @param block 依存を作成するブロック
 * @param depContext 依存コンテキスト
 */
class Dep<T, S: DepScope<S>>(
    depProviderFactory: DepProviderFactory,
    block: suspend context(DepContext<S>)
        () -> T,
    val depContext: DepContext<S>,
) {
    val provider = depProviderFactory.create(block, depContext)

    /**
     * 依存を取得する。
     *
     * @param childContext 子依存コンテキスト（context receiver）
     * @return 依存のインスタンス
     */
    suspend context(childContext: DepContext<S>) fun i(): T {
        depContext.closeHook { childContext.close() }
        childContext.appendParentDepContext(depContext)
        return provider.get(childContext, childContext)
    }
}
