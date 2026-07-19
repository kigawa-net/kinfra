package net.kigawa.kinfra.cli.dep

import net.kigawa.kinfra.TerraformRunner
import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.infra.dep.InfraDeps
import net.kigawa.kinfra.infra.service.TerraformServiceImpl
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
    // ログイン前やterraformブロックの無いプロジェクトではnullになる。ここで例外を投げると
    // `kinfra login`/`kinfra --help`などterraform設定を必要としないコマンドまで起動できなく
    // なるため、nullのままTerraformServiceImplに渡し、実際にterraform操作を行うメソッド側で
    // 必要に応じてエラーを返す。
    val terraformConfig = dep { container.i().terraformRepository.getTerraformConfig() }
    val terraformService = dep {
        TerraformServiceImpl(
            container.i().processExecutor, container.i().terraformRepository,
            container.i().loginRepo,
            container.i().bitwardenSecretManagerRepository,
            terraformConfig.i(),
        )
    }

    // Service layer
    val actionRegistry = dep { ActionRegistry(container.i(), terraformService.i()) }
    val terraformRunner = dep { TerraformRunner(container.i(), actionRegistry.i()) }
    suspend fun main(args: Array<String>) = useDep {
        terraformRunner.i().run(args)
    }
}