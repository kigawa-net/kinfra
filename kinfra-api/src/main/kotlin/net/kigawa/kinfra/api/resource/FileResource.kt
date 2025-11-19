package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext

open class FileResource(
    val filePathResource: FilePathResource,
    val ctx: KinfraContext,
): KinfraResource {
    override suspend fun hash(
        hasher: Hasher,
    ): HashValue {
        return ctx.fileSystem.openReader(filePathResource.path) {
            lineReader {
                hasher.hash(reader = this)
            }
        }
    }

    suspend fun content(): String {
        return ctx.fileSystem.openReader(filePathResource.path) {
            lineReader {
                this.toList().joinToString("\n")
            }
        }

    }
}