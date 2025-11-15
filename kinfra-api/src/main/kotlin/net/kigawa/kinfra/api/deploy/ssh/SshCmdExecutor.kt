package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.cmd.Cmd
import net.kigawa.kinfra.api.cmd.CmdExecutor
import net.kigawa.kinfra.api.cmd.CmdRes
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource

class SshCmdExecutor(
    val localCmdExecutor: CmdExecutor,
    val username: UsernameResource,
    val hostname: HostnameResource,
    val privateKey: FileResource,
): CmdExecutor {
    override fun execute(cmd: Cmd): CmdRes {
        return localCmdExecutor.execute(SshCmd(username, hostname, cmd, privateKey))
    }
}