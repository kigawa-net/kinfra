package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.io.FileReader
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.api.io.FileSystemPath
import net.kigawa.kinfra.api.io.Writer
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.StrCmd
import net.kigawa.kinfra.model.logging.Logger

class SshFileSystem(
    val sshCmdExecutor: SshCmdExecutor,
    val logger: Logger,
): FileSystem {
    override suspend fun exists(path: FileSystemPath): Boolean {
        return sshCmdExecutor.execute(
            ProcessConfig
                .create(StrCmd(listOf("test", "-e", path.strPath)))
                .stdout { forEach { logger.info(it) } }
        ).exitCode == 0
    }

    override suspend fun <T> openReader(
        path: FileSystemPath, block: suspend FileReader.() -> T,
    ): T {
        return sshCmdExecutor.execute(
            ProcessConfig
                .create(StrCmd(listOf("cat", path.strPath)))
                .stdout { SshFileReader(this).block() }
                .stderr { forEach { logger.error(it) } }
        ).outputRes
    }

    override suspend fun <T> openWriter(
        path: FileSystemPath, block: suspend Writer.() -> T,
    ): T {
        return sshCmdExecutor.execute(
            ProcessConfig
                .create(StrCmd(listOf("tee", path.strPath)))
                .stdin { block() }
                .stderr { forEach { logger.error(it) } }
        ).inputRes
    }
}