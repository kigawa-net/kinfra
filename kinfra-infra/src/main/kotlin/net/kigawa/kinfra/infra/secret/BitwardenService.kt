package net.kigawa.kinfra.infra.secret

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.fs.DirResource
import net.kigawa.kinfra.api.fs.FilePathResource
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.StrCmd
import net.kigawa.kinfra.api.resource.NewFileResource
import net.kigawa.kinfra.api.secret.SecretFileResource
import net.kigawa.kinfra.api.secret.SecretResource
import net.kigawa.kinfra.api.secret.SecretService
import net.kigawa.kinfra.model.BitwardenSecret
import net.kigawa.kodel.api.log.getKogger
import net.kigawa.kodel.api.log.traceignore.error

class BitwardenService(
    val cmdExecutor: CmdExecutor,
    val accessToken: SecretResource,
    val secretDir: DirResource,
): SecretService {
    private val gson = Gson()
    val logger = getKogger()
    override suspend fun getSecret(id: String): BitwardenSecret {
        val res = cmdExecutor.execute(
            ProcessConfig
                .create(
                    StrCmd(
                        listOf(
                            "bws", "secret", "get", id, "--access-token", accessToken.value, "--output", "json"
                        )
                    )
                )
                .stderr { forEach { logger.error(it) } }
                .stdout { toList().joinToString(separator = "\n") }
        )
        if (res.exitCode != 0) {
            res.outputRes.split("\n").forEach { logger.error(it) }
            logger.error("Failed to get secret from Bitwarden ${res.exitCode}")
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

    override suspend fun secretFile(id: String, ctx: KinfraContext): SecretFileResource = getSecret(id).let {
        SecretFileResourceImpl(
            it, secretDir, NewFileResource(
                it.value,
                FilePathResource(secretDir.dirPath().path.join(id)),
                ctx
            ).createFile()
        )
    }

}