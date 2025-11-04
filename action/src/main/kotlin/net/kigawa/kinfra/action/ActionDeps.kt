package net.kigawa.kinfra.action

import net.kigawa.kinfra.action.entrypoint.RootEntrypoints
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class ActionDeps<S: ActionScope<S>>(depContext: DepContext<S>): DepsBase<S>(depContext) {
    val rootEntrypoints = dep {
        RootEntrypoints()
    }
}