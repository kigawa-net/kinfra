package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.cmd.Cmd
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource

class SshCmd(
    val username: UsernameResource,
    val hostName: HostnameResource,
    val remoteCmd: Cmd,
): Cmd {

    override val raw: List<String>
        get() = listOf("ssh", "-t","${username.strUsername}@${hostName.strHostname}",remoteCmd.strCmd())
}