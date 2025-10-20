package net.kigawa.kinfra.action.actions
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.message

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

        // Terraform設定が取得できない場合は静かにスキップ
        val config = terraformService.getTerraformConfig()
        if (config == null) {
            return 0
        }

        // プロジェクト名を表示
        println("${AnsiColors.BLUE}Planning Terraform changes for project:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")

        val result = terraformService.plan(args, quiet = false)

        // エラーが発生した場合、プロジェクト情報を表示
        if (result.isFailure()) {
            println("${AnsiColors.RED}Error in project:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")
            result.message()?.let { println("${AnsiColors.RED}Details: $it${AnsiColors.RESET}") }
        }

        // サブプロジェクトでもplanを実行
        val subProjects = subProjectExecutor.getSubProjects()
        if (subProjects.isNotEmpty()) {
            println()
            println("${AnsiColors.BLUE}Found ${subProjects.size} sub-project(s)${AnsiColors.RESET}")

            val subResult = subProjectExecutor.executeInSubProjects(subProjects) { subProject, subProjectDir ->
                println("${AnsiColors.BLUE}Planning Terraform changes for sub-project:${AnsiColors.RESET} ${subProject.name} (${subProjectDir.absolutePath})")

                val process = ProcessBuilder("terraform", "plan")
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

        return result.exitCode()
    }

    override fun getDescription(): String {
        return "Create an execution plan"
    }
}
