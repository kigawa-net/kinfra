package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.api.dep.initializer.DepProvider

/**
 * 依存プロバイダファクトリのインターフェース。
 * 依存プロバイダを作成する。
 */
interface DepProviderFactory {
    /**
     * 依存プロバイダを作成する。
     *
     * @param T 依存の型
     * @param S 依存スコープの型
     * @param block 依存を作成するブロック
     * @param depContext 依存コンテキスト
     * @return 作成された依存プロバイダ
     */
    fun <T, S : DepScope<S>> create(
        block: suspend context(DepContext<S>)
        () -> T,
        depContext: DepContext<S>,
    ): DepProvider<T, S>
}
