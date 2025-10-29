package net.kigawa.kinfra.action.execution

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.sub.SubProject
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

/**
 * サブプロジェクトの実行を管理するクラス
 */
class SubProjectExecutor(
    private val configRepository: ConfigRepository,
    private val loginRepo: LoginRepo
) {

    /**
     * 親プロジェクトの設定からサブプロジェクトリストを取得
     *
     * @return サブプロジェクトのリスト。親プロジェクト設定が存在しない場合は空リスト
     */
    fun getSubProjects(): List<SubProject> {
        val parentConfigPath = loginRepo.kinfraBaseConfigPath().toString()
        if (!configRepository.kinfraParentConfigExists(parentConfigPath)) {
            println("kinfra-parent.yaml not found at $parentConfigPath. Skipping sub-project execution.")
            return emptyList()
        }

        val parentConfig = configRepository.loadKinfraParentConfig(parentConfigPath)
        return parentConfig?.subProjects ?: emptyList()
    }

    /**
     * 親プロジェクトのbackendConfigを取得
     *
     * @return backendConfig。設定が存在しない場合は空のMap
     */
    fun getBackendConfig(): Map<String, Any> {
        val parentConfigPath = loginRepo.kinfraBaseConfigPath().toString()
        if (!configRepository.kinfraParentConfigExists(parentConfigPath)) {
            return emptyMap()
        }

        val parentConfig = configRepository.loadKinfraParentConfig(parentConfigPath)
        return parentConfig?.terraform?.backendConfig ?: emptyMap()
    }

    /**
     * 指定されたサブプロジェクトのマージされたbackendConfigを取得
     * 親プロジェクト(kinfra-parent.yaml)とサブプロジェクト(kinfra.yaml)の設定をマージする
     * サブプロジェクトの設定が親プロジェクトの設定を上書きする
     *
     * @param subProject サブプロジェクト
     * @return マージされたbackendConfig。設定が存在しない場合は空のMap
     */
    fun getMergedBackendConfig(subProject: SubProject): Map<String, Any> {
        // 親プロジェクトのbackendConfigを取得
        val parentBackendConfig = getBackendConfig()
        
        // サブプロジェクトのkinfra.yamlからbackendConfigを取得
        val subProjectDir = loginRepo.repoPath.resolve(subProject.path).toFile()
        val subProjectConfigPath = subProjectDir.resolve("kinfra.yaml")
        
        val subProjectBackendConfig: Map<String, Any> = if (subProjectConfigPath.exists()) {
            val config = configRepository.loadKinfraConfig(subProjectConfigPath.toPath())
            config?.rootProject?.terraform?.backendConfig ?: emptyMap()
        } else {
            emptyMap()
        }
        
        // 親プロジェクトとサブプロジェクトの設定をマージ（サブプロジェクトが優先）
        return parentBackendConfig + subProjectBackendConfig
    }

    /**
     * 各サブプロジェクトでコマンドを実行
     *
     * @param subProjects サブプロジェクトのリスト
     * @param executor サブプロジェクト内で実行する処理
     * @return 最初に失敗したプロジェクトのエラーコード、またはすべて成功した場合は0
     */
    fun executeInSubProjects(
        subProjects: List<SubProject>,
        executor: (SubProject, File) -> Int
    ): Int {

        for ((index, subProject) in subProjects.withIndex()) {
            println()
            println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
            println("${AnsiColors.CYAN}Executing in sub-project [${index + 1}/${subProjects.size}]: ${subProject.name}${AnsiColors.RESET}")
            println("${AnsiColors.CYAN}Path: ${subProject.path}${AnsiColors.RESET}")
            println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
            println()

            val subProjectDir = loginRepo.repoPath.resolve(subProject.path).toFile()
            if (!subProjectDir.exists()) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project directory not found: ${subProjectDir.absolutePath}")
                return 1
            }

            if (!subProjectDir.isDirectory) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Not a directory: ${subProjectDir.absolutePath}")
                return 1
            }

             try {
                 val exitCode = executor(subProject, subProjectDir)
                 if (exitCode != 0) {
                     println()
                     println("${AnsiColors.RED}✗${AnsiColors.RESET} Sub-project ${subProject.name} failed with exit code: $exitCode")
                     return exitCode
                 }

                 println()
                 println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Sub-project ${subProject.name} completed successfully")
             } catch (e: Exception) {
                 println()
                 println("${AnsiColors.RED}✗${AnsiColors.RESET} Sub-project ${subProject.name} failed with exception: ${e.message}")
                 return 1
             }
        }

        return 0
    }
}
