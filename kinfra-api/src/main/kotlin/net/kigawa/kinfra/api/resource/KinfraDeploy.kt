package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.deploy.KinfraContext

interface KinfraDeploy: KinfraResource {
    suspend fun execute(ctx: KinfraContext)
}