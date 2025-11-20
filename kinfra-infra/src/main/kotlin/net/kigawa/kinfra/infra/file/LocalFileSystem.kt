package net.kigawa.kinfra.infra.file

import kotlinx.io.files.SystemFileSystem
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.api.io.FileSystemPath
import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer
import kotlin.io.path.Path

class LocalFileSystem: FileSystem {
    override suspend fun exists(path: FileSystemPath): Boolean {
        return kotlinx.io.files.SystemFileSystem.exists(kotlinx.io.files.Path(path.strPath))
    }

    override suspend fun <T> openReader(
        path: FileSystemPath, block: suspend Reader<String>.() -> T,
    ): T {
        val p = Path(path.strPath)
        return SystemFileSystem.source(p).use { source ->
            val reader = KxLineReader(source)
            reader.block()
        }
    }

    override suspend fun <T> openWriter(
        path: FileSystemPath, block: suspend Writer.() -> T,
    ): T {
        TODO("Not yet implemented")
    }
}