package net.kigawa.kinfra.infra.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.kigawa.kinfra.api.fs.DirPathResource
import net.kigawa.kinfra.api.fs.ExitingDirResource
import net.kigawa.kinfra.api.fs.FileSystem
import net.kigawa.kinfra.api.fs.FileSystemPath
import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer


class LocalFileSystem: FileSystem {
    override suspend fun homeDir(): ExitingDirResource {
        return ExitingDirResource(DirPathResource(System.getProperty("user.home")), this)
    }

    override suspend fun existsFile(path: FileSystemPath): Boolean {
        return withContext(Dispatchers.IO) {
            SystemFileSystem
                .metadataOrNull(kotlinx.io.files.Path(path.strPath))
                ?.isRegularFile
                ?: false
        }
    }

    override suspend fun existsDir(path: FileSystemPath): Boolean {
        return withContext(Dispatchers.IO) {
            SystemFileSystem.exists(kotlinx.io.files.Path(path.strPath))
        }
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
        val p = Path(path.strPath)
        return SystemFileSystem.sink(p).use { sink ->
            val writer = KxLineWriter(sink)
            writer.block()
        }
    }

    override suspend fun createDir(dirPathResource: DirPathResource) {
        withContext(Dispatchers.IO) {
            SystemFileSystem.createDirectories(Path(dirPathResource.path.strPath))
        }
    }
}