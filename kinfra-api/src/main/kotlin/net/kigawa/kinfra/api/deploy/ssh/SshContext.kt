package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.KinfraContext
import net.kigawa.kinfra.api.cmd.CmdExecutor
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource

class SshContext(
    val parent: KinfraContext,
    val localCmdExecutor: CmdExecutor,
    val username: UsernameResource,
    val hostname: HostnameResource,
): KinfraContext {
    override val deployer: Deployer
        get() = parent.deployer
    override val cmdExecutor: CmdExecutor
        get() = SshCmdExecutor(localCmdExecutor, username, hostname)

    override fun childContext(): KinfraContext {
        return SshContext(this, localCmdExecutor, username, hostname)
    }
}