package net.kigawa.kinfra.api.io

interface FileSystem {
    suspend fun <T> openReader(path: FileSystemPath, block: suspend FileReader.() -> T): T
    suspend fun <T> openWriter(path: FileSystemPath, block: suspend Writer.() -> T): T
}