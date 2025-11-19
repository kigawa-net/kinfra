package net.kigawa.iac.cli

import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.core.dep.DefaultDepProviders
import net.kigawa.kodel.core.dep.context.NormalDepCoroutineScope

class IacCliDepsScope(
    override val depCoroutineScope: NormalDepCoroutineScope,
): DepScope<IacCliDepsScope> {
    override val defaultDepProviderFactory: DepProviderFactory
        get() = DefaultDepProviders.Lazy

    companion object {
        fun create(depCoroutineScope: NormalDepCoroutineScope = NormalDepCoroutineScope.create()): IacCliDepsScope {
            return IacCliDepsScope(depCoroutineScope)
        }
    }

    override fun plus(depScope: IacCliDepsScope): IacCliDepsScope {
        return IacCliDepsScope(depCoroutineScope.plus(depScope.depCoroutineScope))
    }

    override fun newDepScope(): IacCliDepsScope {
        return IacCliDepsScope(depCoroutineScope.newScope())
    }

    override fun close() {
        depCoroutineScope.close()
    }
}