package net.kigawa.kodel.dep.context

import net.kigawa.kodel.dep.DepProviderFactory

interface DepScope<S: DepScope<S>> {
    val depProviderFactory: DepProviderFactory
    val depCoroutineScope: DepCoroutineScope

    operator fun plus(depScope: S): S
    fun newDepScope(): S
}