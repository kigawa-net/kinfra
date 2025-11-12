package net.kigawa.iac.model

import net.kigawa.kinfra.api.deploy.KinfraDeployGroup
import net.kigawa.kinfra.api.deploy.ssh.SshDeploy
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource

class KigawaNet: KinfraDeployGroup() {
    val username = UsernameResource("kigawa")
    val hostname = HostnameResource("192.168.1.50")
    val ssh = SshDeploy(username, hostname, listOf()).deploy()
}