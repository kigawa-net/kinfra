package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.ProcessRes
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource

class SshCmdExecutor(
    val localCmdExecutor: CmdExecutor,
    val username: UsernameResource,
    val hostname: HostnameResource,
    val privateKey: FileResource,
): CmdExecutor {

    override fun <SI, SO, SE> execute(
        processConfig: ProcessConfig<SI, SO, SE>,
    ): ProcessRes<SI, SO, SE> {
        return localCmdExecutor.execute(
            processConfig.copy(
                cmd = SshCmd(username, hostname, processConfig.cmd, privateKey)
            )
        )
    }
}