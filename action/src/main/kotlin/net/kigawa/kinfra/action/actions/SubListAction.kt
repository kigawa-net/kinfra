package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors

class SubListAction(
    private val loginRepo: LoginRepo
) : Action {

    override fun execute(args: List<String>): Int {
        val parentConfig = loginRepo.loadKinfraBaseConfig()
        if (parentConfig == null) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration not found")
            println("${AnsiColors.BLUE}No sub-projects configured.${AnsiColors.RESET}")
            return 0
        }

        println("${AnsiColors.BLUE}=== Sub-projects in ${parentConfig.projectName} ===${AnsiColors.RESET}")
        println()

        if (parentConfig.subProjects.isEmpty()) {
            println("${AnsiColors.YELLOW}No sub-projects configured.${AnsiColors.RESET}")
        } else {
            parentConfig.subProjects.forEachIndexed { index, project ->
                println("  ${index + 1}. $project")
            }
        }

        println()
        println("${AnsiColors.BLUE}Total:${AnsiColors.RESET} ${parentConfig.subProjects.size} sub-project(s)")
        println("${AnsiColors.BLUE}Config file:${AnsiColors.RESET} ${parentConfig.filePath}")

        return 0
    }

    override fun getDescription(): String {
        return "List all sub-projects in kinfra-parent.yaml"
    }
}