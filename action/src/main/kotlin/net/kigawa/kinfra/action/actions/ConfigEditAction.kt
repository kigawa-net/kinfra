package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class ConfigEditAction(
    private val loginRepo: LoginRepo,
    private val filePaths: FilePaths
) : Action {

    override fun execute(args: Array<String>): Int {
        val isParentConfig = args.contains("--parent") || args.contains("-p")

        return if (isParentConfig) {
            editParentConfig()
        } else {
            editProjectConfig()
        }
    }

    private fun editProjectConfig(): Int {
        val configPath = try {
            loginRepo.kinfraConfigPath()
        } catch (e: IllegalStateException) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Login configuration not found")
            println("${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra login <github-repo>' first")
            return 1
        }

        val configFile = configPath.toFile()

        // Create sample config if it doesn't exist
        if (!configFile.exists()) {
            println("${AnsiColors.YELLOW}Configuration file not found. Creating from sample...${AnsiColors.RESET}")
            configFile.parentFile?.mkdirs()

            val sampleContent = """
                # Kinfra Project Configuration

                rootProject:
                  projectId: "my-project"
                  description: "My infrastructure project"
                  terraform:
                    version: "1.5.0"
                    workingDirectory: "terraform"

                subProjects: []

                # Optional Bitwarden settings
                # bitwarden:
                #   projectId: "your-bitwarden-project-id"

                # Optional update settings
                # update:
                #   autoUpdate: true
                #   checkInterval: 86400000
                #   githubRepo: "kigawa-net/kinfra"
            """.trimIndent()

            configFile.writeText(sampleContent)
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Created sample configuration at ${configFile.absolutePath}")
        }

        return openInEditor(configFile)
    }

    private fun editParentConfig(): Int {
        val currentDir = File(System.getProperty("user.dir"))
        val configFile = File(currentDir, filePaths.kinfraParentConfigFileName)

        // Create sample config if it doesn't exist
        if (!configFile.exists()) {
            println("${AnsiColors.YELLOW}Parent configuration file not found. Creating from sample...${AnsiColors.RESET}")

            val sampleContent = """
                # Kinfra Parent Project Configuration

                projectName: "my-infrastructure"
                description: "Parent project for managing multiple infrastructure components"

                # Common Terraform settings for all sub-projects
                terraform:
                  version: "1.5.0"
                  workingDirectory: "terraform"

                # List of sub-project paths or identifiers
                subProjects:
                  - "project-a"
                  - "project-b"
                  - "project-c"

                # Optional Bitwarden settings
                # bitwarden:
                #   projectId: "your-bitwarden-project-id"

                # Optional update settings
                # update:
                #   autoUpdate: true
                #   checkInterval: 86400000
                #   githubRepo: "kigawa-net/kinfra"
            """.trimIndent()

            configFile.writeText(sampleContent)
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Created sample parent configuration at ${configFile.absolutePath}")
        }

        return openInEditor(configFile)
    }

    private fun openInEditor(file: File): Int {
        val editor = System.getenv("EDITOR") ?: "vim"

        println("${AnsiColors.BLUE}Opening configuration file in $editor...${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}File:${AnsiColors.RESET} ${file.absolutePath}")
        println()

        return try {
            val process = ProcessBuilder(editor, file.absolutePath)
                .inheritIO()
                .start()

            val exitCode = process.waitFor()

            println()
            if (exitCode == 0) {
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Configuration file edited successfully")
                0
            } else {
                println("${AnsiColors.YELLOW}Editor exited with code $exitCode${AnsiColors.RESET}")
                exitCode
            }
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to open editor: ${e.message}")
            println("${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Set the EDITOR environment variable to your preferred editor")
            println("  Example: export EDITOR=nano")
            1
        }
    }

    override fun getDescription(): String {
        return "Edit kinfra configuration files (use --parent for parent config)"
    }
}
