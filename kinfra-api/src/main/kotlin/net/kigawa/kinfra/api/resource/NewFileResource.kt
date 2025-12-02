package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.fs.FilePathResource
import net.kigawa.kinfra.api.fs.FileResource
import net.kigawa.kinfra.api.hash.HashSrc

class NewFileResource(
    val content: String,
    private val filePathResource: FilePathResource,
    val ctx: KinfraContext,
): KinfraResource {


    suspend fun createFile(): FileResource {
        ctx.fileSystem.openWriter(filePathResource.path) {
            write(content)
        }
        return FileResource(filePathResource, ctx)
    }

    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(filePathResource).str(content)
    }
}