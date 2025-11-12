package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.Hashable
import net.kigawa.kinfra.api.deploy.DeployContext

interface KinfraResource: Hashable {
    suspend fun execute(ctx: DeployContext)
}