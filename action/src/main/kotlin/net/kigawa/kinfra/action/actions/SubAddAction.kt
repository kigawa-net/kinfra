package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class SubAddAction(
    private val configRepository: ConfigRepository,
    private val filePaths: FilePaths
) : Action {

    override fun execute(args: Array<String>): Int {
        if (args.isEmpty()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
            println("Usage: kinfra sub add <project-name>")
            return 1
        }

        val projectName = args[0]
        val currentDir = File(System.getProperty("user.dir"))
        val configFile = File(currentDir, filePaths.kinfraParentConfigFileName)

        if (!configFile.exists()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration file not found: ${configFile.absolutePath}")
            println("${AnsiColors.BLUE}Creating new parent configuration file...${AnsiColors.RESET}")

            // Create default parent config
            val defaultConfig = KinfraParentConfigData(
                projectName = "my-infrastructure",
                description = "Parent project for managing multiple infrastructure components",
                subProjects = emptyList()
            )

            try {
                configRepository.saveKinfraParentConfig(defaultConfig, configFile.absolutePath)
                println("${AnsiColors.GREEN}Created:${AnsiColors.RESET} ${configFile.absolutePath}")
            } catch (e: Exception) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create parent configuration: ${e.message}")
                return 1
            }
        }

        val parentConfig = configRepository.loadKinfraParentConfig(configFile.absolutePath)
        if (parentConfig == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to load parent configuration")
            return 1
        }

        if (parentConfig.subProjects.contains(projectName)) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Sub-project '$projectName' already exists")
            return 0
        }

        // Add the new sub-project
        val updatedConfig = KinfraParentConfigData(
            projectName = parentConfig.projectName,
            description = parentConfig.description,
            terraform = parentConfig.terraform,
            subProjects = parentConfig.subProjects + projectName,
            bitwarden = parentConfig.bitwarden,
            update = parentConfig.update
        )

        try {
            configRepository.saveKinfraParentConfig(updatedConfig, configFile.absolutePath)
            println("${AnsiColors.GREEN}Added:${AnsiColors.RESET} Sub-project '$projectName' to ${configFile.absolutePath}")
            println("${AnsiColors.BLUE}Total sub-projects:${AnsiColors.RESET} ${updatedConfig.subProjects.size}")
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