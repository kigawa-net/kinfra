package net.kigawa.kinfra.infra.secret

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.StrCmd
import net.kigawa.kinfra.api.resource.FilePathResource
import net.kigawa.kinfra.api.resource.NewFileResource
import net.kigawa.kinfra.api.secret.SecretFileResource
import net.kigawa.kinfra.api.secret.SecretResource
import net.kigawa.kinfra.api.secret.SecretService
import net.kigawa.kinfra.model.BitwardenSecret
import net.kigawa.kinfra.model.logging.Logger

class BitwardenService(
    val cmdExecutor: CmdExecutor,
    val accessToken: SecretResource,
    val logger: Logger,
    val ctx: KinfraContext,
    val secretDir
): SecretService {
    private val gson = Gson()
    override suspend fun getSecret(id: String): BitwardenSecret {
        val res = ProcessConfig.create(
            StrCmd(
                listOf(
                    "bws", "secret", "get", id, "--access-token", accessToken.value, "--output", "json"
                )
            )
        ).stderr { forEach { logger.info(it) } }
            .stdout { toList().joinToString(separator = "\n") }
            .let { cmdExecutor.execute(it) }
        if (res.exitCode != 0) {
            throw Exception("Failed to get secret from Bitwarden")
        }

        val json = gson.fromJson(res.outputRes, JsonObject::class.java)
        return BitwardenSecret(
            id = json.get("id").asString,
            organizationId = json.get("organizationId")?.asString ?: "",
            projectId = json.get("projectId")?.asString,
            key = json.get("key").asString,
            value = json.get("value").asString,
            note = json.get("note")?.asString ?: "",
            creationDate = json.get("creationDate")?.asString ?: "",
            revisionDate = json.get("revisionDate")?.asString ?: "",
        )
    }

    override suspend fun secretFile(id: String): SecretFileResource = getSecret(id).let {
        SecretFileResourceImpl(it,,ctx,NewFileResource(
            secret.value,
            FilePathResource(secretDir.path.join(secret.key)),
            ctx
        ).createFile())
    }

}