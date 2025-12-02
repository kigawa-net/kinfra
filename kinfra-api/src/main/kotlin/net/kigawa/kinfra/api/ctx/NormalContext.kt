package net.kigawa.kinfra.api.ctx

import net.kigawa.kinfra.api.UserInterface
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.fs.FileSystem
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kodel.api.log.Kogger

class NormalContext(
    override val deployer: Deployer,
    override val cmdExecutor: CmdExecutor,
    override val fileSystem: FileSystem,
    override val kogger: Kogger,
    override val keys: List<String>, override val userInterface: UserInterface,
): KinfraContext {
    override fun childContext(key: String): KinfraContext {
        return NormalContext(
            deployer,
            cmdExecutor,
            fileSystem,
            kogger,
            keys + key, userInterface,
        )
    }
}