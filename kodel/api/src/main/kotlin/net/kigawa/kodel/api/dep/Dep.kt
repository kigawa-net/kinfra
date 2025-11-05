package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

class Dep<T, S: DepScope<S>>(
    depProviderFactory: DepProviderFactory,
    block: suspend context(DepContext<S>) () -> T,
    val depContext: DepContext<S>,
) {
    val provider = depProviderFactory.create(block, depContext)


    suspend context(childContext: DepContext<S>) fun get(): T {
        depContext.closeHook { childContext.close() }
        childContext.appendParentDepScope(depContext.depScope)
        return provider.get(childContext, childContext)
    }

}
