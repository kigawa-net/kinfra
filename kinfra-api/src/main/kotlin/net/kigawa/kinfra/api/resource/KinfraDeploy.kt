package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.deploy.DeployContext

interface KinfraDeploy: KinfraResource {
    suspend fun execute(ctx: DeployContext)
}