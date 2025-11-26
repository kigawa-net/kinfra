package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.resource.KinfraResource

interface DirResource: KinfraResource {
    suspend fun dirPath(): DirPathResource
    fun createSubDir(string: String): CreatedDirResource
}