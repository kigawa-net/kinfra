package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.resource.KinfraResource

interface Deployer {
    val deployRecorder: DeployRecorder
    val hasher: Hasher
    fun createContext(): DeployContext
    suspend fun deploy(kinfraResource: KinfraResource) {
        val ctx = createContext()
        val hash = hasher.hash(kinfraResource.hashSrc())
        deployRecorder.record(hash) {
            kinfraResource.execute(ctx)
        }
    }
}