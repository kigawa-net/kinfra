package net.kigawa.kinfra.cli

import net.kigawa.kinfra.cli.dep.InfraScopeImpl
import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.infrastructure.dep.InfraDeps
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase
import net.kigawa.kodel.core.dep.DefaultDepProviders

class CliDeps(depContext: DepContext<CliScope>): DepsBase<CliScope>(depContext) {
    val infraDeps = dep {
        InfraDeps(childContext { InfraScopeImpl(it) })
    }
    val container = dep { DependencyContainer() }
    val terraformRunner = dep { container.get().terraformRunner }

    fun main(args: Array<String>) = dep(depProviderFactory = DefaultDepProviders.Singleton) {
        terraformRunner.get().run(args)
    }
}