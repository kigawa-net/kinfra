package net.kigawa.kinfra.infra.dep

import net.kigawa.kinfra.action.ActionDeps
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class InfraDeps<S: InfraScope<S>>(depContext: DepContext<S>): DepsBase<S>(depContext) {
    val actionDeps = dep {
        ActionDeps(childContext { ActionScopeImpl(it) })
    }
}
