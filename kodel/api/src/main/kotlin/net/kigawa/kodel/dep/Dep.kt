package net.kigawa.kodel.dep

import net.kigawa.kodel.dep.context.DepScope
import net.kigawa.kodel.dep.initializer.DepProvider

class Dep<T, S: DepScope<S>>(
    val depInitializer: DepProvider<T,S>,
    val block: suspend context(DepContext<S>) () -> T,
    val depScope: DepContext<S>,
) {
    init {
        depInitializer.init(block,depScope)
    }
    suspend context(baseContext: DepContext<S>) fun get(): T {
        return depInitializer.get(baseContext,depScope) {
            val r = block(it)
            baseContext.appendParentDepScope(it.depScope)
            return@get r
        }
    }

}
