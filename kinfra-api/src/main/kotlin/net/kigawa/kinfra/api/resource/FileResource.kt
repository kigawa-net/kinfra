package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.hash.HashSrc

open class FileResource(
    val filePathResource: FilePathResource,
    val ctx: KinfraContext,
): KinfraResource {


    suspend fun content(): String {
        return ctx.fileSystem.openReader(filePathResource.path) {
            this.toList().joinToString("\n")
        }

    }

    override suspend fun hashSrc() = HashSrc.resource(filePathResource)
        .block { hasher ->
            ctx.fileSystem.openReader(filePathResource.path) {
                forEach {
                    hasher.hash(it)
                }
            }
        }
}