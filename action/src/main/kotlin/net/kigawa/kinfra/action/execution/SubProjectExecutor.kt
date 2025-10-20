package net.kigawa.kinfra.action.execution

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.conf.SubProject
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

/**
 * サブプロジェクトの実行を管理するクラス
 */
class SubProjectExecutor(
    private val configRepository: ConfigRepository
) {

    /**
     * 親プロジェクトの設定からサブプロジェクトリストを取得
     *
     * @param parentConfigPath 親プロジェクト設定ファイルのパス (デフォルト: kinfra-parent.yaml)
     * @return サブプロジェクトのリスト。親プロジェクト設定が存在しない場合は空リスト
     */
    fun getSubProjects(parentConfigPath: String = "kinfra-parent.yaml"): List<SubProject> {
        if (!configRepository.kinfraParentConfigExists(parentConfigPath)) {
            println("kinfra-parent.yaml not found. Skipping sub-project execution.")
            return emptyList()
        }

        val parentConfig = configRepository.loadKinfraParentConfig(parentConfigPath)
        return parentConfig?.subProjects ?: emptyList()
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
        val currentDir = File(".").absoluteFile

        for ((index, subProject) in subProjects.withIndex()) {
            println()
            println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
            println("${AnsiColors.CYAN}Executing in sub-project [${index + 1}/${subProjects.size}]: ${subProject.name}${AnsiColors.RESET}")
            println("${AnsiColors.CYAN}Path: ${subProject.path}${AnsiColors.RESET}")
            println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
            println()

            val subProjectDir = File(subProject.path)
            if (!subProjectDir.exists()) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project directory not found: ${subProject.path}")
                return 1
            }

            if (!subProjectDir.isDirectory) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Not a directory: ${subProject.path}")
                return 1
            }

            // サブプロジェクトディレクトリに移動
            System.setProperty("user.dir", subProjectDir.absolutePath)

            try {
                val exitCode = executor(subProject, subProjectDir)
                if (exitCode != 0) {
                    println()
                    println("${AnsiColors.RED}✗${AnsiColors.RESET} Sub-project ${subProject.name} failed with exit code: $exitCode")
                    return exitCode
                }

                println()
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Sub-project ${subProject.name} completed successfully")
            } finally {
                // 元のディレクトリに戻る
                System.setProperty("user.dir", currentDir.absolutePath)
            }
        }

        return 0
    }
}
