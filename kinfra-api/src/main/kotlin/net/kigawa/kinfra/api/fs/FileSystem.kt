package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer

interface FileSystem {
    suspend fun homeDir(): ExitingDirResource
    suspend fun existsFile(path: FileSystemPath): Boolean
    suspend fun existsDir(path: FileSystemPath): Boolean
    suspend fun <T> openReader(path: FileSystemPath, block: suspend Reader<String>.() -> T): T
    suspend fun <T> openWriter(path: FileSystemPath, block: suspend Writer.() -> T): T
    suspend fun createDir(dirPathResource: DirPathResource)
}