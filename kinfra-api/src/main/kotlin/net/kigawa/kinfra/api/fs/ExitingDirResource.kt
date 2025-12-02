package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.hash.HashSrc

class ExitingDirResource(
    private val dirPathResource: DirPathResource,
    val fileSystem: FileSystem,
): DirResource {

    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(dirPathResource)
    }

    override suspend fun dirPath(): DirPathResource {
        if (!fileSystem.existsDir(dirPathResource.path))
            throw NoSuchElementException("dir ${dirPathResource.path} not found")
        return dirPathResource
    }

    fun joinToExitingDir(string: String): ExitingDirResource {
        return ExitingDirResource(dirPathResource.join(string), fileSystem)
    }

    override fun createSubDir(string: String): CreatedDirResource {
        return CreatedDirResource(dirPathResource.join(string), fileSystem)
    }
}