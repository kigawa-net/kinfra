package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource
import net.kigawa.kinfra.model.logging.Logger

class SshContext(
    val parent: KinfraContext,
    val localCmdExecutor: CmdExecutor,
    val username: UsernameResource,
    val hostname: HostnameResource,
    val privateKey: FileResource,
    override val logger: Logger,
    override val keys: List<String>,
): KinfraContext {
    override val deployer: Deployer
        get() = parent.deployer
    override val cmdExecutor: SshCmdExecutor
        get() = SshCmdExecutor(localCmdExecutor, username, hostname, privateKey)
    override val fileSystem: FileSystem
        get() = SshFileSystem(cmdExecutor, logger)

    override fun childContext(key: String): KinfraContext {
        return SshContext(this, localCmdExecutor, username, hostname, privateKey, logger, keys + key)
    }
}