package net.kigawa.iac.cli

import net.kigawa.iac.model.KigawaNet
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.infra.r2.R2DeployRecorder
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class IacCliDeps(depContext: DepContext<IacCliDepsScope>): DepsBase<IacCliDepsScope>(depContext) {
    val r2Recorder = dep {
        R2DeployRecorder()
    }
    val kinfraContext = dep {
        KinfraContext.create(
            r2Recorder.get(),
        )
    }
    val kigawaNet = dep {
        KigawaNet()
    }

    suspend fun main() = useDep { }
}