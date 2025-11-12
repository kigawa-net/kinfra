package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.resource.KinfraDeploy

interface Deployer {
    val deployRecorder: DeployRecorder
    val hasher: Hasher
    fun createContext(): KinfraContext
    suspend fun deploy(kinfraDeploy: KinfraDeploy) {
        val ctx = createContext()
        val hash = kinfraDeploy.hash(hasher, ctx)
        deployRecorder.record(hash) {
            kinfraDeploy.execute(ctx)
        }
    }
}