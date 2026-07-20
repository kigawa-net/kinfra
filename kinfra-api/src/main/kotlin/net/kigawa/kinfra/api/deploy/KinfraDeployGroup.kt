package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase
import net.kigawa.kodel.api.dep.context.DepScope

abstract class KinfraDeployGroup<D: DepScope<D>>(
    depContext: DepContext<DeployGroupDepScope<D>>,
): DepsBase<DeployGroupDepScope<D>>(depContext), KinfraDeploy {
    val kinfraCtx get() = depContext.depScope.ctx
    abstract suspend fun deploy(): List<KinfraDeploy>
    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(deploy())
    }

    override suspend fun execute(ctx: KinfraContext) {
        deploy().forEach { resource ->
            ctx.deployer.deploy(resource, ctx.childContext(resource.name))
        }
    }
}