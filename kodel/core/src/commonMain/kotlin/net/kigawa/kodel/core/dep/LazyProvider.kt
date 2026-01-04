package net.kigawa.kodel.core.dep

import kotlinx.coroutines.CoroutineStart
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.api.dep.initializer.DepProvider
import net.kigawa.kodel.core.dep.context.async

/**
 * レイジー依存プロバイダ。
 * 初回アクセス時に依存を作成する。
 *
 * @param T 依存の型
 * @param S 依存スコープの型
 * @param block 依存を作成するブロック
 * @param depContext 依存コンテキスト
 */
class LazyProvider<T, S : DepScope<S>>(
    val block: suspend context(DepContext<S>)
    () -> T,
    val depContext: DepContext<S>,
) : DepProvider<T, S> {
    val deferred = depContext.depScope.depCoroutineScope.async(start = CoroutineStart.LAZY) { block(depContext) }

    /**
     * 依存を取得する。
     * 初回アクセス時に作成される。
     *
     * @param baseContext ベースコンテキスト
     * @param depScope 依存スコープコンテキスト
     * @return 依存のインスタンス
     */
    override suspend fun get(
        baseContext: DepContext<S>,
        depScope: DepContext<S>,
    ): T {
        return deferred.await()
    }
}
