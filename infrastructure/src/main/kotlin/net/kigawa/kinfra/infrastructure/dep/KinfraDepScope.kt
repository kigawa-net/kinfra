package net.kigawa.kinfra.infrastructure.dep

import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepScope
import net.kigawa.kodel.core.dep.DefaultDepProviders
import net.kigawa.kodel.core.dep.context.NormalDepCoroutineScope

class KinfraDepScope(
    override val depCoroutineScope: NormalDepCoroutineScope,
): DepScope<KinfraDepScope> {
    override val depProviderFactory: DepProviderFactory
        get() = DefaultDepProviders.Lazy

    override fun plus(
        depScope: KinfraDepScope,
    ): KinfraDepScope {
        return KinfraDepScope(depCoroutineScope.plus(depScope.depCoroutineScope))
    }

    override fun newDepScope(): KinfraDepScope {
        return KinfraDepScope(depCoroutineScope.newScope())
    }
}