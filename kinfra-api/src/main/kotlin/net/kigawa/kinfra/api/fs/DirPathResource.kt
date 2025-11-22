package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.PathResource

class DirPathResource(
    strPath: String,
): PathResource {
    val path = FileSystemPath(strPath)

    override suspend fun hashSrc() = HashSrc.str(path.strPath)
}