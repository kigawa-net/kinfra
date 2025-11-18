package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.io.FileSystemPath

class FilePathResource(
    strPath: String,
): KinfraResource {
    val path = FileSystemPath(strPath)

    init {
        require(strPath.isNotEmpty()) { "path is empty" }
        require(!strPath.endsWith("/")) { "path must not end with /" }
    }

    override suspend fun hash(hasher: Hasher): HashValue {
        return hasher.hash(listOf(path.strPath))
    }
}