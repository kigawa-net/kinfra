package net.kigawa.kodel.api.dep.initializer

import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.context.DepScope

/**
 * 依存プロバイダのインターフェース。
 * 依存のインスタンスを取得する。
 *
 * @param T 依存の型
 * @param S 依存スコープの型
 */
interface DepProvider<T, S : DepScope<S>> {
    /**
     * 依存のインスタンスを取得する。
     *
     * @param baseContext ベースコンテキスト
     * @param depScope 依存スコープコンテキスト
     * @return 依存のインスタンス
     */
    suspend fun get(
        baseContext: DepContext<S>,
        depScope: DepContext<S>,
    ): T
}
