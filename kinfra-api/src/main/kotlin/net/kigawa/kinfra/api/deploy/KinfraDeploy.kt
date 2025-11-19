package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.resource.KinfraResource

interface KinfraDeploy: KinfraResource {
    val name: String
    suspend fun execute(ctx: KinfraContext)
}