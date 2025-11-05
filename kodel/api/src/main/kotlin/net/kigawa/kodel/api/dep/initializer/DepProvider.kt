package net.kigawa.kodel.api.dep.initializer

import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.context.DepScope

interface DepProvider<T, S : DepScope<S>> {
    suspend fun get(
        baseContext: DepContext<S>,
        depScope: DepContext<S>,
    ): T
}
