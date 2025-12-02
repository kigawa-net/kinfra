package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.PathResource

class DirPathResource(
    strPath: String,
): PathResource {
    val path = FileSystemPath(strPath)

    override suspend fun hashSrc() = HashSrc.str(path.strPath)
    fun join(string: String): DirPathResource {
        return DirPathResource(path.strPath.let {
            if (it.endsWith("/")) it else "$it/"
        } + string.let {
            if (it.startsWith("/")) it.substring(1) else it
        })
    }

    fun exitingDir(fileSystem: FileSystem): ExitingDirResource {
        return ExitingDirResource(this, fileSystem)
    }

    fun createDir(fileSystem: FileSystem): CreatedDirResource {
        return CreatedDirResource(this, fileSystem)
    }

    fun joinToFilePath(string: String): FilePathResource {
        return FilePathResource(path.join(string))
    }
}