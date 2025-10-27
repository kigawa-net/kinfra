package net.kigawa.kinfra.action.actions
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.message
import java.io.File

import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.action.execution.SubProjectExecutor
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.isFailure

class PlanAction(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper,
    private val subProjectExecutor: SubProjectExecutor
) : Action {
    override fun execute(args: List<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        // Terraform設定を取得
        val config = terraformService.getTerraformConfig()

        // 親プロジェクトのplanを実行（設定がある場合のみ）
        if (config != null) {
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
        } else {
            println("${AnsiColors.YELLOW}No Terraform configuration found for parent project, skipping${AnsiColors.RESET}")
        }

        // サブプロジェクトでもplanを実行
        val subProjects = subProjectExecutor.getSubProjects()
        if (subProjects.isNotEmpty()) {
            println()
            println("${AnsiColors.BLUE}Found ${subProjects.size} sub-project(s)${AnsiColors.RESET}")

            val subResult = subProjectExecutor.executeInSubProjects(subProjects) { subProject, subProjectDir ->
                println("${AnsiColors.BLUE}Planning Terraform changes for sub-project:${AnsiColors.RESET} ${subProject.name} (${subProjectDir.absolutePath})")

                // サブプロジェクトのマージされたbackendConfigを読み込み
                val backendConfig = subProjectExecutor.getMergedBackendConfig(subProject)

                // サブプロジェクトでもplan前にinitを実行
                println("${AnsiColors.BLUE}Initializing Terraform for sub-project...${AnsiColors.RESET}")
                val initArgs = mutableListOf("terraform", "init", "-input=false")

                // backendConfigから-backend-configオプションを追加
                backendConfig.forEach { (key, value) ->
                    initArgs.add("-backend-config=$key=$value")
                }

                val initProcess = ProcessBuilder(initArgs)
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

                // backendConfigから-backend-configオプションを追加
                backendConfig.forEach { (key, value) ->
                    planArgs.add("-backend-config=$key=$value")
                }

                val process = ProcessBuilder(planArgs)
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

        // 親プロジェクトの結果を返す（設定がない場合は0）
        return config?.let { terraformService.plan(args).exitCode() } ?: 0
    }

    override fun getDescription(): String {
        return "Create an execution plan"
    }
}
