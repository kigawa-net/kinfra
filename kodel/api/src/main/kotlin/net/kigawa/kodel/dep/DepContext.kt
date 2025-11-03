package net.kigawa.kodel.dep

import net.kigawa.kodel.dep.context.DepScope

class DepContext<S: DepScope<S>>(
    var depScope: S,
    val depProviderFactory: DepProviderFactory,
) {
    fun appendParentDepScope(depScope: S) {
        this.depScope += depScope
    }

    fun newDepContext(): DepContext<S> {
       return DepContext(depScope.newDepScope(),depProviderFactory)
    }
}
