package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.cmd.StrCmd
import net.kigawa.kinfra.api.io.FileReader
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.api.io.FileSystemPath
import net.kigawa.kinfra.api.io.Writer

class SshFileSystem(
    val sshCmdExecutor: SshCmdExecutor,
): FileSystem {
    override suspend fun <T> openReader(
        path: FileSystemPath, block: suspend FileReader.() -> T,
    ): T {
        sshCmdExecutor.execute(StrCmd(listOf("cat", path.strPath))).let {
            return it.reader { SshFileReader(this).block() }
        }
    }

    override suspend fun <T> openWriter(
        path: FileSystemPath, block: suspend Writer.() -> T,
    ): T {
        sshCmdExecutor.execute(StrCmd(listOf("tee", path.strPath))).let {
            return it.writer { block() }
        }
    }
}