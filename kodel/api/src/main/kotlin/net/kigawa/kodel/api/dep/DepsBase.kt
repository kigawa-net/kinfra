package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

/**
 * 依存管理のベースクラス。
 * 依存の作成と使用、子コンテキストの管理を提供する。
 *
 * @param S 依存スコープの型
 * @param depContext 依存コンテキスト
 */
abstract class DepsBase<S: DepScope<S>>(
    val depContext: DepContext<S>,
) {
    /**
     * 依存を作成する。
     *
     * @param depProviderFactory 依存プロバイダファクトリ
     * @param block 依存を作成するブロック
     * @return 作成された依存
     */
    inline fun <reified T> dep(
        depProviderFactory: DepProviderFactory = depContext.depScope.defaultDepProviderFactory,
        noinline block: suspend context (DepContext<S>)
            () -> T,
    ): Dep<T, S> {
        return Dep(depProviderFactory, block, depContext.newDepContext(T::class))
    }

    /**
     * 依存を使用して処理を実行する。
     *
     * @param block 実行するブロック
     * @return ブロックの結果
     */
    suspend fun <T> useDep(
        block: suspend context (DepContext<S>)
            () -> T,
    ): T {
        return block(depContext)
    }

    /**
     * 子コンテキストを作成する。
     *
     * @param block 子スコープを作成するブロック
     * @return 子依存コンテキスト
     */
    suspend context(depContext: DepContext<S>) fun <T: DepScope<T>> childContext(
        block: suspend (S) -> T,
    ): DepContext<T> {
        return DepContext(
            block(depContext.depScope), depContext.depClass,
        ).also { closeHook { it.close() } }
    }

    /**
     * クローズフックを追加する。
     *
     * @param block クローズ時に実行するブロック
     */
    context(childContext: DepContext<S>) fun closeHook(block: suspend () -> Unit) {
        childContext.closeHook(block)
    }
}
