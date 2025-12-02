package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.hash.Hasher

interface Deployer {
    val deployRecorder: DeployRecorder
    fun createHasher(): Hasher
    suspend fun deploy(kinfraDeploy: KinfraDeploy, ctx: KinfraContext) {
        val hash = createHasher().apply { hash(kinfraDeploy) }.result()
        deployRecorder.recordPreExec(hash, ctx)
        kinfraDeploy.execute(ctx)
        deployRecorder.recordExecuted(hash, ctx)
    }

}