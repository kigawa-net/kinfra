package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.name

class ConfigEditAction(
    private val loginRepo: LoginRepo,
    val logger: Logger,
): Action {

    override fun execute(args: List<String>): Int {
        // Check for add-subproject subcommand
        if (args.isNotEmpty() && args[0] == "add-subproject") {
            logger.debug("add-subproject subcommand found: $args")
            return addSubProject(args.drop(1).toTypedArray())
        }

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
        } catch (_: IllegalStateException) {
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
            println(
                "${AnsiColors.GREEN}✓${AnsiColors.RESET} Created sample configuration at ${configFile.absolutePath}"
            )
        }

        return openInEditor(configFile)
    }

    private fun editParentConfig(): Int {
        val path = loginRepo.kinfraParentConfigPath()
        // Create sample config if it doesn't exist
        if (!path.exists()) {
            println(
                "${AnsiColors.YELLOW}Parent configuration file not found. Creating from sample...${AnsiColors.RESET}"
            )

            val sampleContent = """
                # Kinfra Parent Project Configuration

                projectName: "my-infrastructure"
                description: "Parent project for managing multiple infrastructure components"

                # Common Terraform settings for all sub-projects
                #terraform:
                #  version: "1.5.0"
                #  workingDirectory: "terraform"

                # List of sub-project paths or identifiers
                #subProjects:
                #  - "project-a"
                #  - "project-b"
                #  - "project-c"

                # Optional Bitwarden settings
                # bitwarden:
                #   projectId: "your-bitwarden-project-id"

                # Optional update settings
                # update:
                #   autoUpdate: true
                #   checkInterval: 86400000
                #   githubRepo: "kigawa-net/kinfra"
            """.trimIndent()

            path.toFile().writeText(sampleContent)
            println(
                "${AnsiColors.GREEN}✓${AnsiColors.RESET} Created sample parent configuration at ${path.absolute()}"
            )
        }

        return openInEditor(path.toFile())
    }

    private fun openInEditor(file: File): Int {
        val editor = findAvailableEditor()

        if (editor == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} No suitable editor found")
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Set the EDITOR environment variable to your preferred editor"
            )
            println("  Example: export EDITOR=nano")
            println()
            println("${AnsiColors.BLUE}Or install one of:${AnsiColors.RESET} nano, vim, vi, emacs")
            return 1
        }

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
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Set the EDITOR environment variable to your preferred editor"
            )
            println("  Example: export EDITOR=nano")
            1
        }
    }

    private fun findAvailableEditor(): String? {
        // First check EDITOR environment variable
        val editorEnv = System.getenv("EDITOR")
        if (editorEnv != null && isCommandAvailable(editorEnv)) {
            return editorEnv
        }

        // Try common editors in order of preference
        val commonEditors = listOf("nano", "vim", "vi", "emacs", "pico")
        for (editor in commonEditors) {
            if (isCommandAvailable(editor)) {
                return editor
            }
        }

        return null
    }

    private fun isCommandAvailable(command: String): Boolean {
        return try {
            val process = ProcessBuilder("which", command)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            exitCode == 0
        } catch (_: Exception) {
            false
        }
    }

    private fun addSubProject(args: Array<String>): Int {
        if (args.isEmpty()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
            println()
            println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
            println("  kinfra config add-subproject <project-name>")
            println()
            println("${AnsiColors.BLUE}Example:${AnsiColors.RESET}")
            println("  kinfra config add-subproject project-a")
            return 1
        }

        val subProjectName = args[0]
        val parentConfig = loginRepo.loadKinfraParentConfig() ?: let {

            println(
                "${AnsiColors.YELLOW}Parent configuration not found. Creating new parent config...${AnsiColors.RESET}"
            )
            print("${AnsiColors.GREEN}Enter parent project name:${AnsiColors.RESET} ")
            val projectName = readlnOrNull()?.trim() ?: "my-infrastructure"

            print("${AnsiColors.GREEN}Enter project description (optional):${AnsiColors.RESET} ")
            val description = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() }

            loginRepo.createKinfraParentConfig(
                KinfraParentConfigData(
                    projectName = projectName,
                    description = description,
                    subProjects = emptyList()
                )
            )
        }

        println(
            "parent config: $parentConfig, subProjectName: $subProjectName, " +
                "args: ${args.joinToString(",")}"
        )
        // Check if sub-project already exists
        if (parentConfig.subProjects.contains(subProjectName)) {
            println(
                "${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Sub-project '$subProjectName' already exists in parent config"
            )
            return 0
        }

        // Add sub-project
        val updatedConfig = KinfraParentConfigData(
            projectName = parentConfig.projectName,
            description = parentConfig.description,
            terraform = parentConfig.terraform,
            subProjects = parentConfig.subProjects + subProjectName,
            bitwarden = parentConfig.bitwarden,
            update = parentConfig.update
        )

        // Save config
        try {
            parentConfig.saveData(updatedConfig)
            println(
                "${AnsiColors.GREEN}✓${AnsiColors.RESET} Sub-project '$subProjectName' added to ${parentConfig.filePath.name}"
            )
            println()
            println("${AnsiColors.BLUE}Current sub-projects:${AnsiColors.RESET}")
            updatedConfig.subProjects.forEachIndexed { index, project ->
                println("  ${index + 1}. $project")
            }
            println()
            println("${AnsiColors.BLUE}Config file:${AnsiColors.RESET} ${parentConfig.filePath.absolute()}")
            return 0
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save parent configuration: ${e.message}")
            return 1
        }
    }

    override fun getDescription(): String {
        return "Edit kinfra configuration files or manage parent project (use --parent to edit parent config, add-subproject to add sub-projects)"
    }

    override fun showHelp() {
        println("${AnsiColors.BLUE}Description:${AnsiColors.RESET}")
        println("  Edit kinfra configuration files or manage parent project")
        println()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra config [options]")
        println("  kinfra config add-subproject <project-name>")
        println()
        println("${AnsiColors.BLUE}Options:${AnsiColors.RESET}")
        println("  --parent, -p   Edit parent project configuration (kinfra-parent.yaml)")
        println()
        println("${AnsiColors.BLUE}Subcommands:${AnsiColors.RESET}")
        println("  add-subproject <name>   Add a sub-project to parent configuration")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  kinfra config                      Edit project configuration")
        println("  kinfra config --parent             Edit parent configuration")
        println("  kinfra config add-subproject app1  Add 'app1' to parent config")
        println()
    }
}
