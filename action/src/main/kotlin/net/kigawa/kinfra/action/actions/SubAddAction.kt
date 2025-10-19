package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
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

        val projectName = args[0]

        val parentConfig = loginRepo.loadKinfraParentConfig()
        if (parentConfig == null) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration not found")
            println("${AnsiColors.BLUE}Creating new parent configuration...${AnsiColors.RESET}")

            // Create default parent config
            val defaultConfigData = KinfraParentConfigData(
                projectName = "my-infrastructure",
                description = "Parent project for managing multiple infrastructure components",
                subProjects = listOf(projectName)
            )

            try {
                loginRepo.createKinfraParentConfig(defaultConfigData)
                println("${AnsiColors.GREEN}Created:${AnsiColors.RESET} Parent configuration with sub-project '$projectName'")
                return 0
            } catch (e: Exception) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create parent configuration: ${e.message}")
                return 1
            }
        }

        if (parentConfig.subProjects.contains(projectName)) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Sub-project '$projectName' already exists")
            return 0
        }

        // Add the new sub-project
        val currentData = parentConfig.toData()
        val updatedData = currentData.copy(
            subProjects = currentData.subProjects + projectName
        )

        try {
            parentConfig.saveData(updatedData)
            println("${AnsiColors.GREEN}Added:${AnsiColors.RESET} Sub-project '$projectName'")
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