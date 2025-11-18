package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext

interface Deployer {
    val deployRecorder: DeployRecorder
    val hasher: Hasher
    suspend fun deploy(kinfraDeploy: KinfraDeploy, ctx: KinfraContext) {
        val hash = kinfraDeploy.hash(hasher)
        deployRecorder.record(hash) {
            kinfraDeploy.execute(ctx)
        }
    }
}