package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.UserInterface
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.fs.FileSystem
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource
import net.kigawa.kinfra.api.secret.SecretFileResource
import net.kigawa.kodel.api.log.Kogger

class SshContext(
    val parent: KinfraContext,
    val localCmdExecutor: CmdExecutor,
    val username: UsernameResource,
    val hostname: HostnameResource,
    val privateKey: SecretFileResource,
    override val kogger: Kogger,
    override val keys: List<String>, override val userInterface: UserInterface,
): KinfraContext {
    override val deployer: Deployer
        get() = parent.deployer
    override val cmdExecutor: SshCmdExecutor
        get() = SshCmdExecutor(localCmdExecutor, username, hostname, privateKey)
    override val fileSystem: FileSystem
        get() = SshFileSystem(cmdExecutor, kogger)

    override fun childContext(key: String): KinfraContext {
        return SshContext(
            this, localCmdExecutor, username, hostname, privateKey, kogger, keys + key,
            userInterface
        )
    }
}