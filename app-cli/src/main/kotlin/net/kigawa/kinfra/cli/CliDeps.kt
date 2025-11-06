package net.kigawa.kinfra.cli

import net.kigawa.kinfra.cli.dep.InfraScopeImpl
import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.infrastructure.dep.InfraDeps
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class CliDeps(depContext: DepContext<CliScope>): DepsBase<CliScope>(depContext) {
    val infraDeps = dep {
        InfraDeps(childContext { InfraScopeImpl(it) })
    }
    val container = dep { DependencyContainer() }
    val terraformRunner = dep { container.get().terraformRunner }

    suspend fun main(args: Array<String>) = useDep {
        terraformRunner.get().run(args)
    }
}