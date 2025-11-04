package net.kigawa.kinfra.infrastructure.action

import net.kigawa.kinfra.action.ActionScope
import net.kigawa.kinfra.infrastructure.InfraScope
import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepCoroutineScope

class ActionScopeImpl<S: InfraScope<S>>(
    val infraScope: S,
): ActionScope<ActionScopeImpl<S>> {
    override val depProviderFactory: DepProviderFactory
        get() = infraScope.depProviderFactory
    override val depCoroutineScope: DepCoroutineScope
        get() = infraScope.depCoroutineScope

    override fun plus(depScope: ActionScopeImpl<S>): ActionScopeImpl<S> {
        return ActionScopeImpl(infraScope.plus(depScope.infraScope))
    }

    override fun newDepScope(): ActionScopeImpl<S> {
        return ActionScopeImpl(infraScope.newDepScope())
    }
}