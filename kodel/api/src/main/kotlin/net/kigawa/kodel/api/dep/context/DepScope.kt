package net.kigawa.kodel.api.dep.context

import net.kigawa.kodel.api.dep.DepProviderFactory

/**
 * 依存スコープのインターフェース。
 * 依存のライフサイクルとスコープを管理する。
 *
 * @param S スコープの型
 */
interface DepScope<S: DepScope<S>> {
    /** デフォルトの依存プロバイダファクトリ */
    val defaultDepProviderFactory: DepProviderFactory

    /** 依存コルーチンスコープ */
    val depCoroutineScope: DepCoroutineScope

    /**
     * スコープを結合する。
     *
     * @param depScope 結合する依存スコープ
     * @return 結合されたスコープ
     */
    operator fun plus(depScope: S): S

    /**
     * 新しい依存スコープを作成する。
     *
     * @return 新しい依存スコープ
     */
    fun newDepScope(): S

    /**
     * スコープをクローズする。
     */
    fun close()
    override fun toString(): String
}
