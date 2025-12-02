package net.kigawa.kinfra.cli.dep

import net.kigawa.kinfra.infra.dep.InfraScope
import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepCoroutineScope

class InfraScopeImpl(
    val cliScope: CliScope,
): InfraScope<InfraScopeImpl> {
    override val defaultDepProviderFactory: DepProviderFactory
        get() = cliScope.defaultDepProviderFactory
    override val depCoroutineScope: DepCoroutineScope
        get() = cliScope.depCoroutineScope

    override fun plus(depScope: InfraScopeImpl): InfraScopeImpl {
        return InfraScopeImpl(cliScope.plus(depScope.cliScope))
    }

    override fun newDepScope(): InfraScopeImpl {
        return InfraScopeImpl(cliScope.newDepScope())
    }

    override fun close() {
        cliScope.close()
    }

    override fun toString(): String {
        return "InfraScopeImpl(cliScope=$cliScope)"
    }
}
