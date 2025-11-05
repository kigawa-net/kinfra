package net.kigawa.kinfra.cli

import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.core.dep.DefaultDepProviders
import net.kigawa.kodel.core.dep.context.NormalDepCoroutineScope

class CliScope(
    override val depCoroutineScope: NormalDepCoroutineScope,
): DepScope<CliScope> {
    override val defaultDepProviderFactory: DepProviderFactory
        get() = DefaultDepProviders.Lazy

    companion object {
        fun create(depCoroutineScope: NormalDepCoroutineScope = NormalDepCoroutineScope.create()): CliScope {
            return CliScope(depCoroutineScope)
        }
    }

    override fun plus(depScope: CliScope): CliScope {
        return CliScope(depCoroutineScope.plus(depScope.depCoroutineScope))
    }

    override fun newDepScope(): CliScope {
        return CliScope(depCoroutineScope.newScope())
    }

    override fun close() {
        depCoroutineScope.close()
    }
}