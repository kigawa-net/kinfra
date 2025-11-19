package net.kigawa.kinfra.api.ctx

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.deploy.NormalDeployer
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.model.logging.Logger

interface KinfraContext {
    companion object {
        fun create(
            deployRecorder: DeployRecorder, hasher: Hasher,
            cmdExecutor: CmdExecutor, fileSystem: FileSystem, logger: Logger,
        ) = NormalContext(
            NormalDeployer(
                deployRecorder, hasher
            ),
            cmdExecutor,
            fileSystem,
            logger,
            emptyList(),
        )
    }

    val keys: List<String>
    val deployer: Deployer
    val cmdExecutor: CmdExecutor
    val fileSystem: FileSystem
    val logger: Logger
    fun childContext(key: String): KinfraContext
}