package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors

class SubRemoveAction(
    private val loginRepo: LoginRepo
) : Action {

    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
            println("Usage: kinfra sub rm <project-name>")
            return 1
        }

        val subProjectName = args[0].trim()

        val parentConfig = loginRepo.loadKinfraBaseConfig()
        if (parentConfig == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Parent configuration not found")
            println("Use 'kinfra sub add <project-name>' to create a parent configuration first")
            return 1
        }

        // Check if sub-project exists
        val existingSubProject = parentConfig.subProjects.find { it.name == subProjectName }
        if (existingSubProject == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project '$subProjectName' not found")
            println("${AnsiColors.BLUE}Available sub-projects:${AnsiColors.RESET}")
            parentConfig.subProjects.forEach { subProject ->
                val displayText = if (subProject.path == subProject.name) {
                    subProject.name
                } else {
                    "${subProject.name}:${subProject.path}"
                }
                println("  - $displayText")
            }
            return 1
        }

        // Remove the sub-project
        val currentData = parentConfig.toData()
        val updatedData = currentData.copy(
            subProjects = currentData.subProjects.filter { it.name != subProjectName }
        )

        try {
            parentConfig.saveData(updatedData)
            val displayText = if (existingSubProject.path == existingSubProject.name) {
                existingSubProject.name
            } else {
                "${existingSubProject.name}:${existingSubProject.path}"
            }
            println("${AnsiColors.GREEN}Removed:${AnsiColors.RESET} Sub-project '$displayText'")
            println("${AnsiColors.BLUE}Total sub-projects:${AnsiColors.RESET} ${updatedData.subProjects.size}")
            
            if (updatedData.subProjects.isEmpty()) {
                println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} No sub-projects remaining")
            }
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save configuration: ${e.message}")
            return 1
        }

        return 0
    }

    override fun getDescription(): String {
        return "Remove a sub-project from kinfra-parent.yaml"
    }
}