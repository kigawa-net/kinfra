package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.model.conf.BackendConfigResolver
import net.kigawa.kinfra.model.execution.SubProjectChangeFilter
import net.kigawa.kinfra.model.execution.SubProjectChangeFilterFactory
import net.kigawa.kinfra.model.execution.SubProjectExecutor
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message
import kotlinx.coroutines.runBlocking

class PlanAction(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper,
    private val subProjectExecutor: SubProjectExecutor,
    private val bitwardenSecretManagerRepository: BitwardenSecretManagerRepository? = null,
    private val changeFilterFactory: SubProjectChangeFilterFactory? = null,
) : Action {
    override fun execute(args: List<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        // Terraform設定を取得
        val config = terraformService.terraformConfig

        // 親プロジェクトのplanを実行（設定がある場合のみ）
        // プロジェクト名を表示
        println("${AnsiColors.BLUE}Planning Terraform changes for project:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")

        // plan実行前に自動でinitを実行
        println("${AnsiColors.BLUE}Initializing Terraform...${AnsiColors.RESET}")
        val initResult = terraformService.init(emptyList())
        if (initResult.isFailure()) {
            println("${AnsiColors.RED}Terraform init failed for parent project${AnsiColors.RESET}")
            initResult.message()?.let { println("${AnsiColors.RED}Details: $it${AnsiColors.RESET}") }
            return initResult.exitCode()
        }

        val result = terraformService.plan(args)

        // エラーが発生した場合、プロジェクト情報を表示
        if (result.isFailure()) {
            println("${AnsiColors.RED}Error in project:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")
            result.message()?.let { println("${AnsiColors.RED}Details: $it${AnsiColors.RESET}") }
        }

        // 親プロジェクトが失敗した場合でもサブプロジェクトを実行するため、exitCodeは最後に返す

        // サブプロジェクトでもplanを実行（前回apply成功時から変更のあったものだけ）
        val allSubProjects = subProjectExecutor.getSubProjects()
        if (allSubProjects.isNotEmpty()) {
            val changeFilter = createChangeFilter()
            val subProjectsWithDirs = allSubProjects.map { it to subProjectExecutor.resolveSubProjectDir(it) }
            val changedSubProjects =
                runBlocking { changeFilter.filterChanged(subProjectsWithDirs) }
            val skippedCount = allSubProjects.size - changedSubProjects.size

            println()
            println(
                "${AnsiColors.BLUE}Found ${allSubProjects.size} sub-project(s), " +
                    "${changedSubProjects.size} changed (${skippedCount} skipped, no changes detected)${AnsiColors.RESET}",
            )

            if (changedSubProjects.isNotEmpty()) {
                val subResult =
                    subProjectExecutor.executeInSubProjects(changedSubProjects.map { it.first }) { subProject, subProjectDir ->
                        println(
                            "${AnsiColors.BLUE}Planning Terraform changes for sub-project:${AnsiColors.RESET} " +
                                "${subProject.name} (${subProjectDir.absolutePath})",
                        )

                        // サブプロジェクトのマージされたbackendConfigを読み込み、bws()マーカーを解決
                        val backendConfig =
                            BackendConfigResolver.flattenAndResolve(
                                subProjectExecutor.getMergedBackendConfig(subProject),
                                bitwardenSecretManagerRepository,
                            )

                        // サブプロジェクトでもplan前にinitを実行
                        println("${AnsiColors.BLUE}Initializing Terraform for sub-project...${AnsiColors.RESET}")
                        val initArgs = mutableListOf("terraform", "init", "-input=false")
                        backendConfig.forEach { (key, value) ->
                            initArgs.add("-backend-config=$key=$value")
                        }

                        val initProcess =
                            ProcessBuilder(initArgs)
                                .directory(subProjectDir)
                                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                                .redirectError(ProcessBuilder.Redirect.INHERIT)
                                .start()

                        val initExitCode = initProcess.waitFor()
                        if (initExitCode != 0) {
                            println("${AnsiColors.RED}Terraform init failed for sub-project ${subProject.name}${AnsiColors.RESET}")
                            return@executeInSubProjects initExitCode
                        }

                        val planArgs = mutableListOf("terraform", "plan", "-input=false")
                        backendConfig.forEach { (key, value) ->
                            planArgs.add("-backend-config=$key=$value")
                        }

                        val process =
                            ProcessBuilder(planArgs)
                                .directory(subProjectDir)
                                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                                .redirectError(ProcessBuilder.Redirect.INHERIT)
                                .start()

                        process.waitFor()
                    }

                if (subResult != 0) {
                    println("${AnsiColors.RED}Sub-project planning failed${AnsiColors.RESET}")
                    return subResult
                }
            }
        }

        // 親プロジェクトの結果を返す
        return result.exitCode()
    }

    /**
     * 変更検出フィルタを、親プロジェクトのbackendConfig（bws()解決済み）から組み立てる。
     * R2の認証情報が揃っていない場合はfail-open（全サブプロジェクトを対象にする）。
     */
    private fun createChangeFilter(): SubProjectChangeFilter {
        val factory = changeFilterFactory ?: return SubProjectChangeFilter.NOOP
        val resolvedBackendConfig =
            BackendConfigResolver.flattenAndResolve(
                subProjectExecutor.getBackendConfig(),
                bitwardenSecretManagerRepository,
            )
        val parentProjectName = subProjectExecutor.getParentProjectName() ?: return SubProjectChangeFilter.NOOP
        return factory.create(resolvedBackendConfig, parentProjectName)
    }

    override fun getDescription(): String {
        return "Create an execution plan"
    }
}
