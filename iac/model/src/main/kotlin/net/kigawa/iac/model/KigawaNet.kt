package net.kigawa.iac.model

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.deploy.KinfraDeployGroup
import net.kigawa.kinfra.api.deploy.ssh.SshDeploy
import net.kigawa.kinfra.api.resource.FilePathResource
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource
import net.kigawa.kinfra.api.secret.SecretService

class KigawaNet(
    override val name: String,
    ctx: KinfraContext,
    val secretService: SecretService
): KinfraDeployGroup(ctx) {
    val username = UsernameResource("kigawa")
    val hostname = HostnameResource("192.168.1.50")
    val privateKey = FileResource(FilePathResource("C:\\Users\\kigawa\\.ssh\\id_rsa"), ctx)
    val ssh = SshDeploy(username, hostname, privateKey, listOf(), "ssh").deploy()
}