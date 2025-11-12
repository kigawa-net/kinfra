package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.deploy.KinfraContext
import net.kigawa.kinfra.api.deploy.Deployed

abstract class KinfraDeployGroup: KinfraDeploy {
    var resources = listOf<KinfraDeploy>()
        private set

    fun <T: KinfraDeploy> addResource(resource: T): Deployed<T> {
        resources += resource
        return Deployed(resource)
    }

    override fun hash(hasher: Hasher, ctx: KinfraContext): HashValue {
        return hasher.hash("", *resources.map { it.hash(hasher, ctx) }.toTypedArray())
    }

    override suspend fun execute(ctx: KinfraContext) {
        resources.forEach { ctx.deployer.deploy(it) }
    }
}