package net.kigawa.iac.model

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.deploy.DeployGroupDepScope
import net.kigawa.kinfra.api.deploy.KinfraDeployGroup
import net.kigawa.kinfra.api.deploy.ssh.SshDeploy
import net.kigawa.kinfra.api.resource.FilePathResource
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource
import net.kigawa.kinfra.api.secret.SecretService
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.context.DepScope

class KigawaNet<D: DepScope<D>>(
    override val name: String,
    ctx: KinfraContext,
    val secretService: SecretService,
    depScope: D,
): KinfraDeployGroup<D>(DepContext(DeployGroupDepScope(depScope, ctx))) {
    val username = UsernameResource("kigawa")
    val hostname = HostnameResource("192.168.1.50")
    val privateKey = dep { secretService.secretFile("ssh") }

    val ssh = dep {
        SshDeploy(username, hostname, privateKey.i(), listOf(), "ssh")
    }
}