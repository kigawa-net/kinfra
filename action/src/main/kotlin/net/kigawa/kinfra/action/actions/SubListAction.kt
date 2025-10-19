package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class SubListAction(
    private val loginRepo: LoginRepo,
    private val filePaths: FilePaths
) : Action {

    override fun execute(args: Array<String>): Int {
        val parentConfig = loginRepo.loadKinfraParentConfig()
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