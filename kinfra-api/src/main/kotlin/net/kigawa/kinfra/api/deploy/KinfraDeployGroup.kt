package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext

abstract class KinfraDeployGroup: KinfraDeploy {
    var resources = listOf<KinfraDeploy>()
        private set

    fun <T: KinfraDeploy> deployResource(resource: T): Deployed<T> {
        resources += resource
        return Deployed(resource)
    }

    fun <T: KinfraDeploy> T.deploy(): Deployed<T> = deployResource(this@deploy)


    override suspend fun hash(hasher: Hasher, ctx: KinfraContext): HashValue {
        return hasher.hash(
            "",
            resources.map { it.hash(hasher, ctx.childContext()) }
        )
    }

    override suspend fun execute(ctx: KinfraContext) {
        resources.forEach { ctx.deployer.deploy(it, ctx.childContext()) }
    }
}