package net.kigawa.kinfra.api

import net.kigawa.kinfra.api.ctx.KinfraContext

interface DeployRecorder {
    suspend fun recordPreExec(hash: HashValue, ctx: KinfraContext)
    suspend fun recordExecuted(hash: HashValue, ctx: KinfraContext)
}