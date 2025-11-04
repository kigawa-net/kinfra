package net.kigawa.kodel.api.dep.context

import net.kigawa.kodel.api.dep.DepProviderFactory

interface DepScope<S: DepScope<S>> {
    val depProviderFactory: DepProviderFactory
    val depCoroutineScope: DepCoroutineScope

    operator fun plus(depScope: S): S
    fun newDepScope(): S
}