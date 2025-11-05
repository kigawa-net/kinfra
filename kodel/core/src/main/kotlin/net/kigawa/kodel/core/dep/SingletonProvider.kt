package net.kigawa.kodel.core.dep

import kotlinx.coroutines.CoroutineStart
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.api.dep.initializer.DepProvider
import net.kigawa.kodel.core.dep.context.async

class SingletonProvider<T, S : DepScope<S>>(
    val block: suspend context(DepContext<S>)
    () -> T,
    val depContext: DepContext<S>,
) : DepProvider<T, S> {
    val deferred = depContext.depScope.depCoroutineScope.async(start = CoroutineStart.DEFAULT) { block(depContext) }

    override suspend fun get(
        baseContext: DepContext<S>,
        depScope: DepContext<S>,
    ): T {
        return deferred.await()
    }
}
