package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class SubShowAction(
    private val configRepository: ConfigRepository,
    private val filePaths: FilePaths
) : Action {

    override fun execute(args: Array<String>): Int {
        if (args.isEmpty()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
            println()
            println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
            println("  kinfra sub-show <project-name>")
            println()
            println("${AnsiColors.BLUE}Example:${AnsiColors.RESET}")
            println("  kinfra sub-show project-a")
            return 1
        }

        val subProjectName = args[0]
        val currentDir = File(System.getProperty("user.dir"))
        val configFile = File(currentDir, filePaths.kinfraParentConfigFileName)

        // Check if parent config exists
        if (!configFile.exists()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Parent configuration file not found: ${configFile.name}")
            println()
            println("${AnsiColors.BLUE}Tip:${AnsiColors.RESET} Use 'kinfra add-subproject <name>' to create a parent config and add sub-projects")
            return 1
        }

        // Load parent config
        val parentConfig = configRepository.loadKinfraParentConfig(configFile.absolutePath)
        if (parentConfig == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to load parent configuration")
            return 1
        }

        // Check if sub-project exists
        if (!parentConfig.subProjects.contains(subProjectName)) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project '$subProjectName' not found in parent config")
            println()
            println("${AnsiColors.BLUE}Available sub-projects:${AnsiColors.RESET}")
            if (parentConfig.subProjects.isEmpty()) {
                println("  ${AnsiColors.YELLOW}(none)${AnsiColors.RESET}")
            } else {
                parentConfig.subProjects.forEachIndexed { index, project ->
                    println("  ${index + 1}. $project")
                }
            }
            return 1
        }

        // Display sub-project information
        println("${AnsiColors.GREEN}Sub-project:${AnsiColors.RESET} $subProjectName")
        println()

        // Check if sub-project directory exists
        val subProjectDir = File(currentDir, subProjectName)
        if (subProjectDir.exists() && subProjectDir.isDirectory) {
            println("${AnsiColors.BLUE}Directory:${AnsiColors.RESET} ${subProjectDir.absolutePath}")
            println("${AnsiColors.BLUE}Status:${AnsiColors.RESET} ${AnsiColors.GREEN}✓ Directory exists${AnsiColors.RESET}")

            // Check for kinfra.yaml
            val kinfraConfigFile = File(subProjectDir, filePaths.kinfraConfigFileName)
            if (kinfraConfigFile.exists()) {
                println("${AnsiColors.BLUE}Config:${AnsiColors.RESET} ${AnsiColors.GREEN}✓ ${filePaths.kinfraConfigFileName} found${AnsiColors.RESET}")

                // Try to load and display config details
                val kinfraConfig = configRepository.loadKinfraConfig(kinfraConfigFile.absolutePath)
                if (kinfraConfig != null) {
                    println()
                    println("${AnsiColors.BLUE}Configuration Details:${AnsiColors.RESET}")
                    println("  Project ID: ${kinfraConfig.rootProject.projectId}")
                    kinfraConfig.rootProject.description?.let {
                        println("  Description: $it")
                    }
                    kinfraConfig.rootProject.terraform?.let { tf ->
                        println("  Terraform Version: ${tf.version}")
                        println("  Working Directory: ${tf.workingDirectory}")
                    }
                    kinfraConfig.bitwarden?.let {
                        println("  Bitwarden Project ID: ${it.projectId}")
                    }
                }
            } else {
                println("${AnsiColors.BLUE}Config:${AnsiColors.RESET} ${AnsiColors.YELLOW}⚠ ${filePaths.kinfraConfigFileName} not found${AnsiColors.RESET}")
            }

            // Check for main.tf
            val mainTfFile = File(subProjectDir, "main.tf")
            if (mainTfFile.exists()) {
                println("${AnsiColors.BLUE}Terraform:${AnsiColors.RESET} ${AnsiColors.GREEN}✓ main.tf found${AnsiColors.RESET}")
            } else {
                println("${AnsiColors.BLUE}Terraform:${AnsiColors.RESET} ${AnsiColors.YELLOW}⚠ main.tf not found${AnsiColors.RESET}")
            }
        } else {
            println("${AnsiColors.BLUE}Directory:${AnsiColors.RESET} ${subProjectDir.absolutePath}")
            println("${AnsiColors.BLUE}Status:${AnsiColors.RESET} ${AnsiColors.YELLOW}⚠ Directory does not exist${AnsiColors.RESET}")
        }

        println()
        println("${AnsiColors.BLUE}Parent Config:${AnsiColors.RESET} ${configFile.absolutePath}")

        return 0
    }

    override fun getDescription(): String {
        return "Show details of a sub-project"
    }
}
