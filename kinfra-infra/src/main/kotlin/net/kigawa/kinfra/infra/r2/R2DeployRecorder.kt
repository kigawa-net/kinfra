package net.kigawa.kinfra.infra.r2

import com.google.gson.Gson
import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.ctx.KinfraContext
import java.time.Instant


class R2DeployRecorder(
    accountId: String,
    accessKey: String,
    secretKey: String,
    private val bucketName: String,
): DeployRecorder {
    val r2Client = R2Client(accountId, accessKey, secretKey)
    private val deployNode = DeployNode(null, mutableMapOf())

    override suspend fun recordPreExec(hash: HashValue, ctx: KinfraContext) {
        var node = deployNode
        ctx.keys.forEach { key ->
            node = node.children.getOrPut(key) { DeployNode(null, mutableMapOf()) }
        }
        node.record = DeployRecord(
            hash = hash.toString(),
            timestamp = Instant.now().toString(),
            type = "pre"
        )
    }

    override suspend fun recordExecuted(hash: HashValue, ctx: KinfraContext) {
        var node = deployNode
        ctx.keys.forEach { key ->
            node = node.children.getOrPut(key) { DeployNode(null, mutableMapOf()) }
        }
        node.record = DeployRecord(
            hash = hash.toString(),
            timestamp = Instant.now().toString(),
            type = "exec"
        )
    }

    fun close() {
        val gson = Gson()
        val json = gson.toJson(deployNode)
        r2Client.putObject(bucketName, "deploy-record.json", json.toByteArray())
        r2Client.close()
    }
}