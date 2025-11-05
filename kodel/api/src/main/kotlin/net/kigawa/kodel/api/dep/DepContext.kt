package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

class DepContext<S: DepScope<S>>(
    var depScope: S,
) {
    var closeHooks = listOf<suspend () -> Unit>({ depScope.close() })

    fun appendParentDepScope(depScope: S) {
        this.depScope += depScope
        closeHook { depScope.close() }
    }

    fun newDepContext(): DepContext<S> {
        return DepContext(depScope.newDepScope()).also { closeHook { it.close() } }
    }

    fun closeHook(block: suspend () -> Unit) {
        closeHooks += block
    }

    suspend fun close() {
        closeHooks.reversed().forEach {
            try {
                it()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
