package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.io.FileSystemPath

class FilePathResource(
    val path: FileSystemPath,
): PathResource {
    constructor(strPath: String): this(FileSystemPath(strPath))

    init {
        require(path.strPath.isNotEmpty()) { "path is empty" }
        require(!path.strPath.endsWith("/")) { "path must not end with /" }
    }

    override suspend fun hashSrc() = HashSrc.str(path.strPath)
}