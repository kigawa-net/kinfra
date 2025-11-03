package net.kigawa.kodel.dep

import net.kigawa.kodel.dep.context.DepScope
import net.kigawa.kodel.dep.initializer.DepProvider

interface DepProviderFactory {
    fun <T,S: DepScope<S>>create(): DepProvider<T, S>
}