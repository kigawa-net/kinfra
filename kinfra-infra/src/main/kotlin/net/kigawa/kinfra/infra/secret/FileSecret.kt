package net.kigawa.kinfra.infra.secret

import net.kigawa.kinfra.api.UserInterface
import net.kigawa.kinfra.api.fs.FilePathResource
import net.kigawa.kinfra.api.fs.FileSystem
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.KinfraResource
import net.kigawa.kinfra.api.secret.PlainTxtSecret
import net.kigawa.kinfra.api.secret.SecretResource
import net.kigawa.kodel.api.log.getLogger
import net.kigawa.kodel.api.log.traceignore.debug

class FileSecret(
    val filePathResource: FilePathResource,
    val fileSystem: FileSystem,
    val userInterface: UserInterface,
): KinfraResource {
    val logger = getLogger()
    suspend fun readOrType(displayName: String): SecretResource {
        readOrNull()?.let { if (it.value.isNotBlank()) return it }
        fileSystem.createDir(filePathResource.parent())
        val secret = userInterface.askStrLineQuestion("type $displayName")
        fileSystem.openWriter(filePathResource.path) {
            write(secret)
        }
        logger.debug("Secret $displayName saved to ${filePathResource.path}")
        return PlainTxtSecret(secret)
    }

    suspend fun readOrNull(): SecretResource? {
        if (!fileSystem.existsFile(filePathResource.path)) {
            logger.debug("Secret file ${filePathResource.path} not found")
            return null
        }
        logger.debug("Secret file ${filePathResource.path} found")
        return fileSystem.openReader(filePathResource.path) {
            toList().joinToString("\n")
        }.let { PlainTxtSecret(it) }
    }


    override suspend fun hashSrc() = HashSrc.resource(filePathResource, readOrNull())
}