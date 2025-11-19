package net.kigawa.kinfra.api.ctx

import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.model.logging.Logger

class NormalContext(
    override val deployer: Deployer,
    override val cmdExecutor: CmdExecutor,
    override val fileSystem: FileSystem,
    override val logger: Logger,
    override val keys: List<String>,
): KinfraContext {
    override fun childContext(key: String): KinfraContext {
        return NormalContext(
            deployer,
            cmdExecutor,
            fileSystem,
            logger,
            keys + key,
        )
    }
}