package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfigCompleter
import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.global.IncompleteGlobalConfig
import java.nio.file.Path

class GlobalConfigCompleterImpl(
    private val filePaths: FilePaths
) : GlobalConfigCompleter {

    override fun complete(incompleteGlobalConfig: IncompleteGlobalConfig): GlobalConfig {
        // IncompleteGlobalConfig は空のインターフェースなので、
        // 実際には GlobalConfigScheme から変換する
        val scheme = incompleteGlobalConfig as? GlobalConfigScheme
        if (scheme != null) {
            return completeGlobalConfigScheme(scheme)
        }

        // デフォルトの GlobalConfig を返す
        val reposPath = filePaths.baseConfigDir?.resolve(filePaths.reposDir)
            ?: throw IllegalStateException("Config directory not available")
        return GlobalConfigImpl(GlobalConfigScheme(), reposPath)
    }

    private fun completeGlobalConfigScheme(scheme: GlobalConfigScheme): GlobalConfig {
        val reposPath = filePaths.baseConfigDir?.resolve(filePaths.reposDir)
            ?: throw IllegalStateException("Config directory not available")

        val login = scheme.login ?: return GlobalConfigImpl(scheme, reposPath)

        var modified = false
        var repo = login.repo
        var enabledProjects = login.enabledProjects

        // repoが不足している場合
        if (repo.isBlank()) {
            print("リポジトリを入力してください (例: kigawa01/infra): ")
            val input = readlnOrNull()?.trim() ?: ""
            if (input.isNotBlank()) {
                repo = input
                modified = true
            }
        }

        // enabledProjectsが空の場合
        if (enabledProjects.isEmpty()) {
            print("有効にするプロジェクトを入力してください (カンマ区切り): ")
            val input = readlnOrNull()?.trim() ?: ""
            if (input.isNotBlank()) {
                enabledProjects = input.split(',').map { it.trim() }.filter { it.isNotBlank() }
                modified = true
            }
        }

        val completedScheme = if (modified) {
            scheme.copy(
                login = scheme.login?.copy(
                    repo = repo,
                    enabledProjects = enabledProjects
                )
            )
        } else {
            scheme
        }

        return GlobalConfigImpl(completedScheme, reposPath)
    }
}