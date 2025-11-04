package net.kigawa.kinfra.action

import net.kigawa.kodel.api.dep.context.DepScope

interface ActionScope<S: ActionScope<S>>: DepScope<S> {
}