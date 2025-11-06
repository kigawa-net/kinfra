package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

abstract class DepsBase<S: DepScope<S>>(
    val depContext: DepContext<S>,
) {
    fun <T> dep(
        depProviderFactory: DepProviderFactory = depContext.depScope.defaultDepProviderFactory,
        block: suspend context (DepContext<S>)
            () -> T,
    ): Dep<T, S> {
        return Dep(depProviderFactory, block, depContext.newDepContext())
    }

    suspend fun <T> useDep(
        block: suspend context (DepContext<S>)
            () -> T,
    ): T {
        return block(depContext)
    }

    suspend context(depContext: DepContext<S>) fun <T: DepScope<T>> childContext(
        block: suspend (S) -> T,
    ): DepContext<T> {
        return DepContext(
            block(depContext.depScope)
        ).also { closeHook { it.close() } }
    }

    context(childContext: DepContext<S>) fun closeHook(block: suspend () -> Unit) {
        childContext.closeHook(block)
    }
}
