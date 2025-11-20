package net.kigawa.kinfra.api.io

interface FileSystem {
    suspend fun exists(path: FileSystemPath): Boolean
    suspend fun <T> openReader(path: FileSystemPath, block: suspend Reader<String>.() -> T): T
    suspend fun <T> openWriter(path: FileSystemPath, block: suspend Writer.() -> T): T
}