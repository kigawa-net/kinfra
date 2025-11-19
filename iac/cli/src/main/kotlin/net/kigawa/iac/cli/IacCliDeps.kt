package net.kigawa.iac.cli

import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class IacCliDeps(depContext: DepContext<IacCliDepsScope>): DepsBase<IacCliDepsScope>(depContext) {
    suspend fun main() = useDep {  }
}