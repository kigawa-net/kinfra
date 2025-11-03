package net.kigawa.kinfra.infrastructure.dep

import net.kigawa.kodel.dep.DepProviderFactory
import net.kigawa.kodel.dep.context.DepCoroutineScope
import net.kigawa.kodel.dep.context.DepScope

class RootDepScope: DepScope<RootDepScope> {
    override val depProviderFactory: DepProviderFactory
        get() = TODO("Not yet implemented")
    override val depCoroutineScope: DepCoroutineScope
        get() = TODO("Not yet implemented")

    override fun plus(
        depScope: RootDepScope,
    ): RootDepScope {
        TODO("Not yet implemented")
    }

    override fun newDepScope(): RootDepScope {
        TODO("Not yet implemented")
    }
}