package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.logging.Logger
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.sub.SubProject
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class SubEditAction(
    private val loginRepo: LoginRepo,
    private val logger: Logger
) : Action {

    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            showUsage()
            return 1
        }

        val subProjectName = args[0]
        val parentConfig = loginRepo.loadKinfraBaseConfig()

        if (parentConfig == null) {
            println(
                "${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration file not found: ${loginRepo.kinfraBaseConfigPath()}"
            )
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra sub add <project-name>' to create a configuration file"
            )
            return 1
        }

        val subProject = parentConfig.subProjects.find { it.name == subProjectName }
        if (subProject == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project '$subProjectName' not found")
            println("${AnsiColors.BLUE}Available sub-projects:${AnsiColors.RESET}")
            if (parentConfig.subProjects.isEmpty()) {
                println("  ${AnsiColors.YELLOW}(none)${AnsiColors.RESET}")
            } else {
                parentConfig.subProjects.forEach { project ->
                    val displayText = if (project.path == project.name) {
                        project.name
                    } else {
                        "${project.name}:${project.path}"
                    }
                    println("  - $displayText")
                }
            }
            return 1
        }

        return editSubProjectConfig(subProject)
    }

    private fun editSubProjectConfig(subProject: SubProject): Int {
        // Determine the sub-project config file path
        val configFile = if (subProject.path.startsWith('/')) {
            // Absolute path
            File(subProject.path, "kinfra.yaml")
        } else {
            // Relative path - resolve from parent config directory
            val parentConfigDir = loginRepo.kinfraBaseConfigPath().parent
            parentConfigDir.resolve(subProject.path).resolve("kinfra.yaml").toFile()
        }

        // Create sample config if it doesn't exist
        if (!configFile.exists()) {
            createSampleSubProjectConfig(configFile, subProject)
        }

        return openInEditor(configFile)
    }

    private fun createSampleSubProjectConfig(configFile: File, subProject: SubProject) {
        println("${AnsiColors.YELLOW}Configuration file not found. Creating from sample...${AnsiColors.RESET}")
        
        // Ensure parent directory exists
        val parentDir = configFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            val created = parentDir.mkdirs()
            if (!created) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create directory: ${parentDir.absolutePath}")
                return
            }
        }

        val sampleContent = """
            # Kinfra Sub-Project Configuration: ${subProject.name}

            rootProject:
              projectId: "${subProject.name}"
              description: "Sub-project: ${subProject.name}"
              terraform:
                version: "1.5.0"
                workingDirectory: "."

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

    private fun openInEditor(file: File): Int {
        val editor = findAvailableEditor()

        if (editor == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} No suitable editor found")
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Set EDITOR environment variable to your preferred editor"
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
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Set EDITOR environment variable to your preferred editor"
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

    private fun showUsage() {
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
        println()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra sub edit <project-name>")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  kinfra sub edit web-app")
        println("  kinfra sub edit api")
        println()
        println("${AnsiColors.BLUE}Note:${AnsiColors.RESET}")
        println("  Opens the kinfra.yaml file for the specified sub-project in your default editor")
        println("  If the configuration file doesn't exist, a sample will be created")
    }

    override fun getDescription(): String {
        return "Edit configuration of a specific sub-project"
    }
}