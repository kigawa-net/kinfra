package net.kigawa.kinfra.infra.secret

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.fs.FilePathResource
import net.kigawa.kinfra.api.resource.KinfraResource
import net.kigawa.kinfra.api.secret.PlainTxtSecret
import net.kigawa.kinfra.api.secret.SecretResource

class FileSecret(
    val filePathResource: FilePathResource,
    val ctx: KinfraContext,
): KinfraResource {
    suspend fun readOrType(displayName: String): SecretResource {
        readOrNull()?.let { return it }
        val secret = ctx.userInterface.askStrLineQuestion("type $displayName:")
        ctx.fileSystem.openWriter(filePathResource.path) {
            write(secret)
        }
        return PlainTxtSecret(secret)
    }

    suspend fun readOrNull(): SecretResource? {
        if (!ctx.fileSystem.exists(filePathResource.path)) {
            return null
        }
        return ctx.fileSystem.openReader(filePathResource.path) {
            toList().joinToString("\n")
        }.let { PlainTxtSecret(it) }
    }


    override suspend fun hashSrc() = HashSrc.resource(filePathResource, readOrNull())

}