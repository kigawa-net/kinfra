package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.deploy.DeployContext

abstract class KinfraResourceGroup: KinfraResource {
    var resources = listOf<KinfraResource>()
        private set

    fun addResource(resource: KinfraResource) {
        resources += resource
    }

    override fun hashSrc(): String {
        return resources.joinToString(";") { it.hashSrc() }
    }

    override suspend fun execute(ctx: DeployContext) {
        resources.forEach { ctx.deployer.deploy(it) }
    }
}