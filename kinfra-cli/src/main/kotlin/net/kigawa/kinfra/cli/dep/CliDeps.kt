package net.kigawa.kinfra.cli.dep

import net.kigawa.kinfra.TerraformRunner
import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.infrastructure.dep.InfraDeps
import net.kigawa.kinfra.infrastructure.service.TerraformServiceImpl
import net.kigawa.kinfra.service.ActionRegistry
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

/**
 * CLIアプリケーションの依存管理クラス。
 * インフラ依存、コンテナ、Terraformランナーを提供する。
 *
 * @param depContext 依存コンテキスト
 */
class CliDeps(depContext: DepContext<CliScope>): DepsBase<CliScope>(depContext) {
    val infraDeps = dep {
        InfraDeps(childContext { InfraScopeImpl(it) })
    }
    val container = dep { DependencyContainer() }

    // Presentation layer
    val terraformConfig = dep { container.get().terraformRepository.getTerraformConfig() }
    val terraformService = dep {
        TerraformServiceImpl(
            container.get().processExecutor, container.get().terraformRepository,
            container.get().configRepository,
            container.get().bitwardenSecretManagerRepository,
            terraformConfig.get() ?: throw IllegalStateException("Bitwarden secret manager not initialized"),
        )
    }

    // Service layer
    val actionRegistry = dep { ActionRegistry(container.get(), terraformService.get()) }
    val terraformRunner = dep { TerraformRunner(container.get(), actionRegistry.get()) }
    suspend fun main(args: Array<String>) = useDep {
        terraformRunner.get().run(args)
    }
}