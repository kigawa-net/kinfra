package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.hash.HashValue
import net.kigawa.kinfra.api.hash.Hasher
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.hash.HashSrc

abstract class KinfraDeployGroup(
    val ctx: KinfraContext,
): KinfraDeploy {
    var resources = listOf<KinfraDeploy>()
        private set

    fun <T: KinfraDeploy> deployResource(resource: T): Deployed<T> {
        resources += resource
        return Deployed(resource)
    }

    fun <T: KinfraDeploy> T.deploy(): Deployed<T> = deployResource(this@deploy)


    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(resources)
    }

    override suspend fun execute(ctx: KinfraContext) {
        resources.forEach { resource ->
            ctx.deployer.deploy(resource, ctx.childContext(resource.name))
        }
    }
}