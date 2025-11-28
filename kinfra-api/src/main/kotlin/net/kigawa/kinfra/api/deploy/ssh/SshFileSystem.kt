package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.fs.DirPathResource
import net.kigawa.kinfra.api.fs.ExitingDirResource
import net.kigawa.kinfra.api.fs.FileSystem
import net.kigawa.kinfra.api.fs.FileSystemPath
import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.StrCmd
import net.kigawa.kodel.api.log.Logger

class SshFileSystem(
    val sshCmdExecutor: SshCmdExecutor,
    val logger: Logger,
): FileSystem {
    override suspend fun homeDir(): ExitingDirResource {
        return ExitingDirResource(
            DirPathResource(
                sshCmdExecutor.execute(
                    ProcessConfig
                        .create(StrCmd(listOf("pwd")))
                        .stdout { read() ?: "" }
                        .stderr { forEach { logger.error(it) } }
                ).outputRes), this)
    }

    override suspend fun existsFile(path: FileSystemPath): Boolean {
        return sshCmdExecutor.execute(
            ProcessConfig
                .create(StrCmd(listOf("test", "-f", path.strPath)))
                .stdout { forEach { logger.info(it) } }
                .stderr { forEach { logger.error(it) } }
        ).exitCode == 0
    }

    override suspend fun existsDir(path: FileSystemPath): Boolean {
        return sshCmdExecutor.execute(
            ProcessConfig
                .create(StrCmd(listOf("test", "-f", path.strPath)))
                .stdout { forEach { logger.info(it) } }
                .stderr { forEach { logger.error(it) } }
        ).exitCode == 0
    }

    override suspend fun <T> openReader(
        path: FileSystemPath, block: suspend Reader<String>.() -> T,
    ): T {
        return sshCmdExecutor.execute(
            ProcessConfig
                .create(StrCmd(listOf("cat", path.strPath)))
                .stdout { block() }
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

    override suspend fun createDir(dirPathResource: DirPathResource) {
        sshCmdExecutor.execute(
            ProcessConfig
                .create(StrCmd(listOf("mkdir", "-p", dirPathResource.path.strPath)))
                .stderr { forEach { logger.error(it) } }
        )
    }

}