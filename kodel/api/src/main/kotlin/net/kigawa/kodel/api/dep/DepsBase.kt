package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

abstract class DepsBase<S: DepScope<S>>(
    val depContext: DepContext<S>,
) {
    fun <T> dep(
        depProviderFactory: DepProviderFactory = depContext.defaultDepProviderFactory,
        block: suspend context (DepContext<S>)() -> T,
    ): Dep<T, S> {
        return Dep(depProviderFactory, block, depContext.newDepContext())
    }
}