package net.kigawa.kodel.dep

import net.kigawa.kodel.dep.context.DepScope
import net.kigawa.kodel.dep.initializer.DepProvider

class LazyProvider<T,S: DepScope<S>>: DepProvider<T,S> {
    override suspend fun get(
        baseContext: DepContext<S>, depScope: DepContext<S>,
        block: suspend (depContext: DepContext<S>) -> T,
    ): T {
        TODO("Not yet implemented")
    }

    override fun init(
        block: suspend (DepContext<S>) -> T, depContext: DepContext<S>,
    ) {
        TODO("Not yet implemented")
    }
}