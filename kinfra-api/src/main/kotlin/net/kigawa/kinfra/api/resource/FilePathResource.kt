package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.io.FileSystemPath

class FilePathResource(
    strPath: String,
): KinfraResource {
    val path = FileSystemPath(strPath)

    init {
        require(strPath.isNotEmpty()) { "path is empty" }
        require(!strPath.endsWith("/")) { "path must not end with /" }
    }

    override suspend fun hashSrc() = HashSrc.str(path.strPath)
}