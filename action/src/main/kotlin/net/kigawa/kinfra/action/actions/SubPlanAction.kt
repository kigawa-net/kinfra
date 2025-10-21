package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.execution.SubProjectExecutor
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors

class SubPlanAction(
    private val loginRepo: LoginRepo,
    private val subProjectExecutor: SubProjectExecutor
) : Action {

    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            showUsage()
            return 1
        }

        val subProjectName = args[0]
        val parentConfig = loginRepo.loadKinfraBaseConfig()

        if (parentConfig == null) {
            println(
                "${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration file not found: ${loginRepo.kinfraBaseConfigPath()}"
            )
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra sub add <project-name>' to create a configuration file"
            )
            return 1
        }

        val subProject = parentConfig.subProjects.find { it.name == subProjectName }
        if (subProject == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project '$subProjectName' not found")
            println("${AnsiColors.BLUE}Available sub-projects:${AnsiColors.RESET}")
            if (parentConfig.subProjects.isEmpty()) {
                println("  ${AnsiColors.YELLOW}(none)${AnsiColors.RESET}")
            } else {
                parentConfig.subProjects.forEach { project ->
                    val displayText = if (project.path == project.name) {
                        project.name
                    } else {
                        "${project.name}:${project.path}"
                    }
                    println("  - $displayText")
                }
            }
            return 1
        }

        // サブプロジェクトでplanを実行
        val result = subProjectExecutor.executeInSubProjects(listOf(subProject)) { _, subProjectDir ->
            // サブプロジェクトディレクトリでterraform planを実行
            val process = ProcessBuilder("terraform", "plan")
                .directory(subProjectDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

            process.waitFor()
        }

        return result
    }

    override fun getDescription(): String {
        return "Run terraform plan in a sub-project"
    }

    override fun showHelp() {
        println("Usage: kinfra sub plan <sub-project-name>")
        println()
        println("Run terraform plan in the specified sub-project.")
        println()
        println("Arguments:")
        println("  <sub-project-name>  Name of the sub-project")
        println()
        println("Examples:")
        println("  kinfra sub plan my-project")
    }

    private fun showUsage() {
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
        println()
        showHelp()
    }
}