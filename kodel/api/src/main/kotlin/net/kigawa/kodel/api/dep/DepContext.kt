package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

class DepContext<S: DepScope<S>>(
    var depScope: S,
    val defaultDepProviderFactory: DepProviderFactory,
) {
    fun appendParentDepScope(depScope: S) {
        this.depScope += depScope
    }

    fun newDepContext(): DepContext<S> {
        return DepContext(depScope.newDepScope(), defaultDepProviderFactory)
    }
}
