package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.process.Cmd
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource
import net.kigawa.kinfra.api.secret.SecretFileResource

class SshCmd(
    val username: UsernameResource,
    val hostName: HostnameResource,
    val remoteCmd: Cmd,
    val privateKey: SecretFileResource,
): Cmd {

    override val raw: List<String>
        get() = listOf(
            "ssh", "-i", privateKey.fileResource.filePathResource.path.strPath, "-t",
            "${username.strUsername}@${hostName.strHostname}", remoteCmd.strCmd()
        )
}