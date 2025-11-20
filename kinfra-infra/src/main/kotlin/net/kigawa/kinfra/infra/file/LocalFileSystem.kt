package net.kigawa.kinfra.infra.file

import net.kigawa.kinfra.api.io.FileReader
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.api.io.FileSystemPath
import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.exists

class LocalFileSystem: FileSystem {
    override suspend fun exists(path: FileSystemPath): Boolean {
        return Path(path.strPath).exists()
    }

    override suspend fun <T> openReader(
        path: FileSystemPath, block: suspend Reader<String>.() -> T,
    ): T {
        val p = Path(path.strPath)

        FileChannel.open(p, StandardOpenOption.READ).use { channel ->
            channel.read(byteBuffer)
        }
    }

    override suspend fun <T> openWriter(
        path: FileSystemPath, block: suspend Writer.() -> T,
    ): T {
        TODO("Not yet implemented")
    }
}