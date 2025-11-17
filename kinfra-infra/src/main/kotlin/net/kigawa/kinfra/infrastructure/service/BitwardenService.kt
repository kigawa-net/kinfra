package net.kigawa.kinfra.infrastructure.service

import com.google.gson.JsonObject
import net.kigawa.kinfra.api.SecretService
import net.kigawa.kinfra.api.cmd.CmdExecutor
import net.kigawa.kinfra.api.cmd.StrCmd
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.model.BitwardenSecret

class BitwardenService(
    val cmdExecutor: CmdExecutor,
    val accessToken: FileResource,
): SecretService {
    fun getSecret(id: String): BitwardenSecret {
        val result =
            cmdExecutor.execute(
                StrCmd(listOf("bws", "secret", "get", id, "--access-token", accessToken.content, "--output", "json"))
            )

        if (result.exitCode != 0) {
            return null
        }

        val json = gson.fromJson(result.output, JsonObject::class.java)
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
}