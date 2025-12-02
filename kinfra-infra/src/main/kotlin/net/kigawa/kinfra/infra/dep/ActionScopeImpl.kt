package net.kigawa.kinfra.infra.dep

import net.kigawa.kinfra.action.ActionScope
import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.api.dep.context.DepCoroutineScope

class ActionScopeImpl<S : InfraScope<S>>(
    val infraScope: S,
) : ActionScope<ActionScopeImpl<S>> {
    override val defaultDepProviderFactory: DepProviderFactory
        get() = infraScope.defaultDepProviderFactory
    override val depCoroutineScope: DepCoroutineScope
        get() = infraScope.depCoroutineScope

    override fun plus(depScope: ActionScopeImpl<S>): ActionScopeImpl<S> {
        return ActionScopeImpl(infraScope.plus(depScope.infraScope))
    }

    override fun newDepScope(): ActionScopeImpl<S> {
        return ActionScopeImpl(infraScope.newDepScope())
    }

    override fun close() {
        infraScope.close()
    }

    override fun toString(): String {
        return "ActionScopeImpl(infraScope=$infraScope)"
    }
}