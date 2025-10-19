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
            println("Usage: kinfra sub show <project-name>")
            return 1
        }

        val projectName = args[0]
        val currentDir = File(System.getProperty("user.dir"))
        val configFile = File(currentDir, filePaths.kinfraParentConfigFileName)

        if (!configFile.exists()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Parent configuration file not found: ${configFile.absolutePath}")
            println("${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra sub add <project-name>' to create a configuration file")
            return 1
        }

        val parentConfig = configRepository.loadKinfraParentConfig(configFile.absolutePath)
        if (parentConfig == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to load parent configuration")
            return 1
        }

        if (!parentConfig.subProjects.contains(projectName)) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project '$projectName' not found")
            println("${AnsiColors.BLUE}Available sub-projects:${AnsiColors.RESET}")
            if (parentConfig.subProjects.isEmpty()) {
                println("  ${AnsiColors.YELLOW}(none)${AnsiColors.RESET}")
            } else {
                parentConfig.subProjects.forEach { project ->
                    println("  - $project")
                }
            }
            return 1
        }

        // Display sub-project details
        println("${AnsiColors.BLUE}=== Sub-project: $projectName ===${AnsiColors.RESET}")
        println()

        // Check if sub-project directory exists
        val projectDir = File(currentDir, projectName)
        val dirExists = projectDir.exists() && projectDir.isDirectory

        println("${AnsiColors.BLUE}Name:${AnsiColors.RESET} $projectName")
        println("${AnsiColors.BLUE}Directory:${AnsiColors.RESET} ${projectDir.absolutePath}")
        println("${AnsiColors.BLUE}Status:${AnsiColors.RESET} ${if (dirExists) "${AnsiColors.GREEN}Directory exists${AnsiColors.RESET}" else "${AnsiColors.YELLOW}Directory not found${AnsiColors.RESET}"}")

        if (dirExists) {
            // Check for kinfra.yaml in sub-project
            val subProjectConfigFile = File(projectDir, filePaths.kinfraConfigFileName)
            if (subProjectConfigFile.exists()) {
                println("${AnsiColors.BLUE}Config file:${AnsiColors.RESET} ${AnsiColors.GREEN}Found${AnsiColors.RESET} (${subProjectConfigFile.name})")

                val subProjectConfig = configRepository.loadKinfraConfig(subProjectConfigFile.absolutePath)
                if (subProjectConfig != null) {
                    println()
                    println("${AnsiColors.BLUE}--- Configuration details ---${AnsiColors.RESET}")
                    println("${AnsiColors.BLUE}Project ID:${AnsiColors.RESET} ${subProjectConfig.rootProject.projectId}")

                    val description = subProjectConfig.rootProject.description
                    if (description != null) {
                        println("${AnsiColors.BLUE}Description:${AnsiColors.RESET} $description")
                    }

                    val terraform = subProjectConfig.rootProject.terraform
                    if (terraform != null) {
                        println("${AnsiColors.BLUE}Terraform version:${AnsiColors.RESET} ${terraform.version}")
                        println("${AnsiColors.BLUE}Terraform working directory:${AnsiColors.RESET} ${terraform.workingDirectory}")
                    }

                    val bitwarden = subProjectConfig.bitwarden
                    if (bitwarden != null) {
                        println("${AnsiColors.BLUE}Bitwarden project ID:${AnsiColors.RESET} ${bitwarden.projectId}")
                    }
                }
            } else {
                println("${AnsiColors.BLUE}Config file:${AnsiColors.RESET} ${AnsiColors.YELLOW}Not found${AnsiColors.RESET}")
            }

            // Count files in directory
            val files = projectDir.listFiles()
            if (files != null) {
                val fileCount = files.count { it.isFile }
                val dirCount = files.count { it.isDirectory }
                println("${AnsiColors.BLUE}Contents:${AnsiColors.RESET} $fileCount file(s), $dirCount directory(ies)")
            }
        }

        println()
        println("${AnsiColors.BLUE}Parent config:${AnsiColors.RESET} ${configFile.absolutePath}")
        println("${AnsiColors.BLUE}Total sub-projects:${AnsiColors.RESET} ${parentConfig.subProjects.size}")

        return 0
    }

    override fun getDescription(): String {
        return "Show details of a specific sub-project"
    }
}
