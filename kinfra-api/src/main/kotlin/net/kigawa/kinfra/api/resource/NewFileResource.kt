package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.ctx.KinfraContext

class NewFileResource(
    val content: String,
    private val filePathResource: FilePathResource,
    val ctx: KinfraContext,
): KinfraResource {
    override suspend fun hash(hasher: Hasher): HashValue {
        return hasher.hash(listOf(content))
    }

    suspend fun createFile(): FileResource {
        ctx.fileSystem.openWriter(filePathResource.path){
            write(content)
        }
        return FileResource(filePathResource, ctx)
    }
}