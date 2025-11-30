package net.kigawa.kinfra.infra.secret

import net.kigawa.kinfra.api.UserInterface
import net.kigawa.kinfra.api.fs.FilePathResource
import net.kigawa.kinfra.api.fs.FileSystem
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.KinfraResource
import net.kigawa.kinfra.api.secret.PlainTxtSecret
import net.kigawa.kinfra.api.secret.SecretResource

class FileSecret(
    val filePathResource: FilePathResource,
    val fileSystem: FileSystem,
    val userInterface: UserInterface,
): KinfraResource {
    suspend fun readOrType(displayName: String): SecretResource {
        readOrNull()?.let { return it }
        fileSystem.createDir(filePathResource.parent())
        val secret = userInterface.askStrLineQuestion("type $displayName")
        fileSystem.openWriter(filePathResource.path) {
            write(secret)
        }
        return PlainTxtSecret(secret)
    }

    suspend fun readOrNull(): SecretResource? {
        if (!fileSystem.existsFile(filePathResource.path)) {
            return null
        }
        return fileSystem.openReader(filePathResource.path) {
            toList().joinToString("\n")
        }.let { PlainTxtSecret(it) }
    }


    override suspend fun hashSrc() = HashSrc.resource(filePathResource, readOrNull())
}