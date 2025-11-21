package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.io.FileSystemPath

class DirPathResource(
    strPath: String,
): PathResource {
    val path = FileSystemPath(strPath)

    override suspend fun hashSrc() = HashSrc.str(path.strPath)
}