package net.kigawa.iac.cli

import net.kigawa.iac.model.KigawaNet
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class IacCliDeps(depContext: DepContext<IacCliDepsScope>): DepsBase<IacCliDepsScope>(depContext) {
    val kinfraContext = dep {
        KinfraContext.create()
    }
    val kigawaNet = dep {
        KigawaNet()
    }

    suspend fun main() = useDep { }
}