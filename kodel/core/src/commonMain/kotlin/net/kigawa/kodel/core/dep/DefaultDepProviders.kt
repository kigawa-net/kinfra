package net.kigawa.kodel.core.dep

import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.api.dep.initializer.DepProvider

/**
 * デフォルトの依存プロバイダを提供するオブジェクト。
 */
object DefaultDepProviders {
    /**
     * シングルトンプロバイダファクトリ。
     */
    object Singleton : DepProviderFactory {
        override fun <T, S : DepScope<S>> create(
            block: suspend context(DepContext<S>)
            () -> T,
            depContext: DepContext<S>,
        ): DepProvider<T, S> {
            return SingletonProvider(block, depContext)
        }
    }

    /**
     * レイジープロバイダファクトリ。
     */
    object Lazy : DepProviderFactory {
        override fun <T, S : DepScope<S>> create(
            block: suspend context(DepContext<S>)
            () -> T,
            depContext: DepContext<S>,
        ): DepProvider<T, S> {
            return LazyProvider(block, depContext)
        }
    }
}
