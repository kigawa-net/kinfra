package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.deploy.KinfraDeploy
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource

class SshDeploy(
    val username: UsernameResource,
    val host: HostnameResource,
    val privateKey: FileResource,
    val deploys: List<KinfraDeploy>,
    override val name: String,
): KinfraDeploy {
    fun createCtx(ctx: KinfraContext): SshContext = SshContext(
        ctx, ctx.cmdExecutor,
        username, host, privateKey, ctx.logger, ctx.keys + name
    )

    override suspend fun execute(ctx: KinfraContext) {
        deploys.forEach { ctx.deployer.deploy(it, createCtx(ctx)) }
    }

    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(host, username).resource(deploys)
    }
}