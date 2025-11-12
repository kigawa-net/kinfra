package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.deploy.DeployContext
import net.kigawa.kinfra.api.deploy.Deployed

abstract class KinfraDeployGroup: KinfraDeploy {
    var resources = listOf<KinfraDeploy>()
        private set

    fun <T: KinfraDeploy> addResource(resource: T): Deployed<T> {
        resources += resource
        return Deployed(resource)
    }

    override fun hash(hasher: Hasher): HashValue {
        return hasher.hash("", *resources.map { it.hash(hasher) }.toTypedArray())
    }

    override suspend fun execute(ctx: DeployContext) {
        resources.forEach { ctx.deployer.deploy(it) }
    }
}