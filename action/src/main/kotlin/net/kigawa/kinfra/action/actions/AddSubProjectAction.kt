package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.infrastructure.config.KinfraParentConfigScheme
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class AddSubProjectAction(
    private val configRepository: ConfigRepository,
    private val filePaths: FilePaths
) : Action {

    override fun execute(args: Array<String>): Int {
        if (args.isEmpty()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
            println()
            println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
            println("  kinfra add-subproject <project-name>")
            println()
            println("${AnsiColors.BLUE}Example:${AnsiColors.RESET}")
            println("  kinfra add-subproject project-a")
            return 1
        }

        val subProjectName = args[0]
        val currentDir = File(System.getProperty("user.dir"))
        val configFile = File(currentDir, filePaths.kinfraParentConfigFileName)

        // Load or create parent config
        val parentConfig = if (configFile.exists()) {
            val loaded = configRepository.loadKinfraParentConfig(configFile.absolutePath)
            if (loaded == null) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to load parent configuration")
                return 1
            }
            loaded as KinfraParentConfigScheme
        } else {
            println("${AnsiColors.YELLOW}Parent configuration not found. Creating new parent config...${AnsiColors.RESET}")
            print("${AnsiColors.GREEN}Enter parent project name:${AnsiColors.RESET} ")
            val projectName = readlnOrNull()?.trim() ?: "my-infrastructure"

            print("${AnsiColors.GREEN}Enter project description (optional):${AnsiColors.RESET} ")
            val description = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() }

            KinfraParentConfigScheme(
                projectName = projectName,
                description = description,
                subProjects = emptyList()
            )
        }

        // Check if sub-project already exists
        if (parentConfig.subProjects.contains(subProjectName)) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Sub-project '$subProjectName' already exists in parent config")
            return 0
        }

        // Add sub-project
        val updatedConfig = parentConfig.copy(
            subProjects = parentConfig.subProjects + subProjectName
        )

        // Save config
        try {
            configRepository.saveKinfraParentConfig(updatedConfig, configFile.absolutePath)
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Sub-project '$subProjectName' added to ${configFile.name}")
            println()
            println("${AnsiColors.BLUE}Current sub-projects:${AnsiColors.RESET}")
            updatedConfig.subProjects.forEachIndexed { index, project ->
                println("  ${index + 1}. $project")
            }
            println()
            println("${AnsiColors.BLUE}Config file:${AnsiColors.RESET} ${configFile.absolutePath}")
            return 0
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save parent configuration: ${e.message}")
            return 1
        }
    }

    override fun getDescription(): String {
        return "Add a sub-project to kinfra-parent.yaml"
    }
}
