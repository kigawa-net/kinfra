package net.kigawa.kodel.dep.initializer

import net.kigawa.kodel.dep.DepContext
import net.kigawa.kodel.dep.context.DepScope

interface DepProvider<T,S: DepScope<S>> {
    suspend fun get(
        baseContext: DepContext<S>, depScope: DepContext<S>, block: suspend (depContext: DepContext<S>) -> T,
    ): T

    fun init(block: suspend (DepContext<S>) -> T, depContext: DepContext<S>)
}