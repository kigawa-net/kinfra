package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import java.io.File

class LocalFileResource(
    filePathResource: FilePathResource,
): KinfraResource {
    val file: File = filePathResource.path.toFile()
    val content get() = file.readText()
    override fun hash(hasher: Hasher): HashValue {
        return hasher.hash(content)
    }
}