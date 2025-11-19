package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.ctx.KinfraContext

interface Deployer {
    val deployRecorder: DeployRecorder
    val hasher: Hasher
    suspend fun deploy(kinfraDeploy: KinfraDeploy, ctx: KinfraContext) {
        val hash = kinfraDeploy.hash(hasher)
        deployRecorder.recordPreExec(hash, ctx)
        kinfraDeploy.execute(ctx)
        deployRecorder.recordExecuted(hash, ctx)
    }

}