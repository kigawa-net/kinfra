package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext

class FileResource(
    val filePathResource: FilePathResource,
): KinfraResource {
    override suspend fun hash(
        hasher: Hasher, ctx: KinfraContext,
    ): HashValue {
        return ctx.fileSystem.openReader(filePathResource.path) {
            lineReader {
                hasher.hash(reader = this)
            }
        }
    }
}