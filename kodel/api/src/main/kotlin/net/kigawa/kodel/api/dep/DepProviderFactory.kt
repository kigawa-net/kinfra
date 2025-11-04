package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.api.dep.initializer.DepProvider

interface DepProviderFactory {
    fun <T, S: DepScope<S>> create(
        block: suspend context(DepContext<S>) () -> T, depContext: DepContext<S>,
    ): DepProvider<T, S>
}