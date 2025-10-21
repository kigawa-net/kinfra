package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
import net.kigawa.kinfra.model.SubProjectImpl
import net.kigawa.kinfra.model.util.AnsiColors

class SubAddAction(
    private val loginRepo: LoginRepo
) : Action {

    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
            println("Usage: kinfra sub add <project-name>")
            return 1
        }

        val subProjectInput = args[0]
        val subProject = if (':' in subProjectInput) {
            val parts = subProjectInput.split(':', limit = 2)
            SubProjectImpl(parts[0].trim(), parts[1].trim())
        } else {
            SubProjectImpl(subProjectInput.trim())
        }

        val parentConfig = loginRepo.loadKinfraParentConfig()
        if (parentConfig == null) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration not found")
            println("${AnsiColors.BLUE}Creating new parent configuration...${AnsiColors.RESET}")

            // Create default parent config
            val defaultConfigData = KinfraParentConfigData(
                projectName = "my-infrastructure",
                description = "Parent project for managing multiple infrastructure components",
                subProjects = listOf(subProject)
            )

            try {
                loginRepo.createKinfraParentConfig(defaultConfigData)
                val displayText = if (subProject.path == subProject.name) {
                    subProject.name
                } else {
                    "${subProject.name}:${subProject.path}"
                }
                println("${AnsiColors.GREEN}Created:${AnsiColors.RESET} Parent configuration with sub-project '$displayText'")
                return 0
            } catch (e: Exception) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create parent configuration: ${e.message}")
                return 1
            }
        }

        if (parentConfig.subProjects.any { it.name == subProject.name }) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Sub-project '${subProject.name}' already exists")
            return 0
        }

        // Add the new sub-project
        val currentData = parentConfig.toData()
        val updatedData = currentData.copy(
            subProjects = currentData.subProjects + subProject
        )

        try {
            parentConfig.saveData(updatedData)
            val displayText = if (subProject.path == subProject.name) {
                subProject.name
            } else {
                "${subProject.name}:${subProject.path}"
            }
            println("${AnsiColors.GREEN}Added:${AnsiColors.RESET} Sub-project '$displayText'")
            println("${AnsiColors.BLUE}Total sub-projects:${AnsiColors.RESET} ${updatedData.subProjects.size}")
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save configuration: ${e.message}")
            return 1
        }

        return 0
    }

    override fun getDescription(): String {
        return "Add a new sub-project to kinfra-parent.yaml"
    }
}