package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepCoroutineScope
import net.kigawa.kodel.api.dep.context.DepScope

class DeployGroupDepScope<T: DepScope<T>>(
    val parent: T,
    val ctx: KinfraContext,
): DepScope<DeployGroupDepScope<T>> {
    override val defaultDepProviderFactory: DepProviderFactory
        get() = parent.defaultDepProviderFactory
    override val depCoroutineScope: DepCoroutineScope
        get() = parent.depCoroutineScope

    override fun plus(
        depScope: DeployGroupDepScope<T>,
    ): DeployGroupDepScope<T> {
        return DeployGroupDepScope(parent.plus(depScope.parent), ctx)
    }

    override fun newDepScope(): DeployGroupDepScope<T> {
        return DeployGroupDepScope(parent.newDepScope(), ctx)
    }

    override fun close() {
    }

    override fun toString(): String {
        return "DeployGroupDepScope(parent=$parent, ctx=$ctx)"
    }
}