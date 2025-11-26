package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.NewFileResource

class CreatedDirResource(
    private val dirPathResource: DirPathResource,
    val fileSystem: FileSystem,
): DirResource {
    override suspend fun dirPath(): DirPathResource {
        fileSystem.createDir(dirPathResource)
        return dirPathResource
    }

    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(dirPathResource)
    }

    override fun createSubDir(string: String): CreatedDirResource {
        return CreatedDirResource(dirPathResource.join(string), fileSystem)
    }

    fun childFilePath(string: String): FilePathResource {
        return dirPathResource.joinToFilePath(string)
    }
}