package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext
import net.kigawa.kinfra.api.deploy.KinfraDeploy
import net.kigawa.kinfra.api.resource.HostnameResource
import net.kigawa.kinfra.api.resource.UsernameResource

class SshDeploy(
    val username: UsernameResource,
    val host: HostnameResource,
    val deploys: List<KinfraDeploy>,
): KinfraDeploy {
    fun createCtx(ctx: KinfraContext): SshContext = SshContext(
        ctx, ctx.cmdExecutor,
        username, host
    )

    override suspend fun execute(ctx: KinfraContext) {
        deploys.forEach { ctx.deployer.deploy(it, createCtx(ctx)) }
    }

    override fun hash(
        hasher: Hasher, ctx: KinfraContext,
    ): HashValue {
        return hasher.hash(
            "",
            listOf(
                host.hash(hasher, ctx.childContext()),
                username.hash(hasher, ctx.childContext())
            ) + deploys.map { it.hash(hasher, createCtx(ctx)) }
        )
    }
}