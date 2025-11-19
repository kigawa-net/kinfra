package net.kigawa.kinfra.infra.r2

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.ctx.KinfraContext

class R2DeployRecorder(
    accountId: String,
    accessKey: String,
    secretKey: String,
): DeployRecorder {
    val r2Client = R2Client(accountId, accessKey, secretKey)

    override suspend fun recordPreExec(hash: HashValue, ctx: KinfraContext) {
        TODO("Not yet implemented")
    }

    override suspend fun recordExecuted(hash: HashValue, ctx: KinfraContext) {
        TODO("Not yet implemented")
    }
    fun close(){
    }
}