package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.deploy.KinfraContext
import java.io.File

class LocalFileResource(
    filePathResource: FilePathResource,
): KinfraResource {
    val file: File = filePathResource.path.toFile()
    override fun hash(hasher: Hasher, ctx: KinfraContext): HashValue {
        return hasher.hash(file.readText())
    }
}