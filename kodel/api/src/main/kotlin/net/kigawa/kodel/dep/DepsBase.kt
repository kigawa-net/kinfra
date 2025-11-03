package net.kigawa.kodel.dep

import net.kigawa.kodel.dep.context.DepScope
import net.kigawa.kodel.dep.initializer.DepProvider

abstract class DepsBase<S: DepScope<S>>(
    val depContext: DepContext<S>
) {
    fun <T> dep(
        depInitializer: DepProvider<T, S> = depContext.depProviderFactory.create(),
        block: suspend context (DepContext<S>)() -> T,
    ): Dep<T, S> {
        return Dep(depInitializer, block, depContext.newDepContext())
    }
}