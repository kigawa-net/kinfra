package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class SubListAction(
    private val configRepository: ConfigRepository,
    private val filePaths: FilePaths
) : Action {

    override fun execute(args: Array<String>): Int {
        val currentDir = File(System.getProperty("user.dir"))
        val configFile = File(currentDir, filePaths.kinfraParentConfigFileName)

        if (!configFile.exists()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration file not found: ${configFile.absolutePath}")
            println("${AnsiColors.BLUE}No sub-projects configured.${AnsiColors.RESET}")
            return 0
        }

        val parentConfig = configRepository.loadKinfraParentConfig(configFile.absolutePath)
        if (parentConfig == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to load parent configuration")
            return 1
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
        println("${AnsiColors.BLUE}Config file:${AnsiColors.RESET} ${configFile.absolutePath}")

        return 0
    }

    override fun getDescription(): String {
        return "List all sub-projects in kinfra-parent.yaml"
    }
}