package net.kigawa.kodel.dep

import kotlinx.coroutines.CompletableDeferred
import net.kigawa.kodel.dep.context.DepScope
import net.kigawa.kodel.dep.initializer.DepProvider

class SingletonProvider<T, S: DepScope<S>>: DepProvider<T, S> {

    private val dep = CompletableDeferred<T>()
    override suspend fun get(
        baseContext: DepContext<S>, depScope: DepContext<S>,
        block: suspend (depContext: DepContext<S>) -> T,
    ): T {
        return dep.await()
    }

    override fun init(
        block: suspend (DepContext<S>) -> T,
        depContext: DepContext<S>,
    ) {
        depContext.depScope.depCoroutineScope.launch {
            dep.complete(block(depContext))
        }
    }
}