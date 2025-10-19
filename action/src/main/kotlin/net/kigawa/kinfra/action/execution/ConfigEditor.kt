package net.kigawa.kinfra.action.execution

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
import net.kigawa.kinfra.model.conf.SubProject
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File
import kotlin.io.path.exists
import kotlin.io.path.absolute

/**
 * 設定ファイル編集の各機能を担当するクラス
 */
class ConfigEditor(
    private val loginRepo: LoginRepo,
    private val logger: Logger
) {
    
    fun editProjectConfig(): Int {
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
            createSampleProjectConfig(configFile)
        }

        return openInEditor(configFile)
    }
    
    fun editParentConfig(): Int {
        val path = loginRepo.kinfraParentConfigPath()
        
        // Create sample config if it doesn't exist
        if (!path.exists()) {
            createSampleParentConfig(path)
        }

        return openInEditor(path.toFile())
    }
    
    fun addSubProject(args: Array<String>): Int {
        return SubProjectManager(loginRepo, logger).addSubProject(args)
    }
    
    private fun createSampleProjectConfig(configFile: File) {
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
    
    private fun createSampleParentConfig(path: java.nio.file.Path) {
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
}

/**
 * サブプロジェクト管理を担当するクラス
 */
private class SubProjectManager(
    private val loginRepo: LoginRepo,
    private val logger: Logger
) {
    
    fun addSubProject(args: Array<String>): Int {
        if (args.isEmpty()) {
            showUsage()
            return 1
        }

        val subProjectInput = args[0]
        val subProject = if (':' in subProjectInput) {
            // "name:path" format
            val parts = subProjectInput.split(':', limit = 2)
            SubProject(parts[0].trim(), parts[1].trim())
        } else {
            // Just name, use name as path
            SubProject(subProjectInput.trim())
        }
        
        val parentConfig = getOrCreateParentConfig() ?: return 1

        // Check if sub-project already exists
        if (parentConfig.subProjects.any { it.name == subProject.name }) {
            println(
                "${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Sub-project '${subProject.name}' already exists in parent config"
            )
            return 0
        }

        // Add sub-project
        val updatedConfig = KinfraParentConfigData(
            projectName = parentConfig.projectName,
            description = parentConfig.description,
            terraform = parentConfig.terraform,
            subProjects = parentConfig.subProjects + subProject,
            bitwarden = parentConfig.bitwarden,
            update = parentConfig.update
        )

        return saveConfig(updatedConfig, subProject, parentConfig)
    }
    
    private fun getOrCreateParentConfig(): KinfraParentConfig? {
        val existingConfig = loginRepo.loadKinfraParentConfig()
        if (existingConfig != null) {
            return existingConfig
        }

        println(
            "${AnsiColors.YELLOW}Parent configuration not found. Creating new parent config...${AnsiColors.RESET}"
        )
        print("${AnsiColors.GREEN}Enter parent project name:${AnsiColors.RESET} ")
        val projectName = readlnOrNull()?.trim() ?: "my-infrastructure"

        print("${AnsiColors.GREEN}Enter project description (optional):${AnsiColors.RESET} ")
        val description = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() }

        return loginRepo.createKinfraParentConfig(
            KinfraParentConfigData(
                projectName = projectName,
                description = description,
                subProjects = emptyList()
            )
        )
    }
    
    private fun saveConfig(
        updatedConfig: KinfraParentConfigData,
        subProject: SubProject,
        parentConfig: KinfraParentConfig
    ): Int {
        return try {
            parentConfig.saveData(updatedConfig)
            val displayText = if (subProject.path == subProject.name) {
                subProject.name
            } else {
                "${subProject.name}:${subProject.path}"
            }
            println(
                "${AnsiColors.GREEN}✓${AnsiColors.RESET} Sub-project '$displayText' added to parent configuration"
            )
            println()
            println("${AnsiColors.BLUE}Current sub-projects:${AnsiColors.RESET}")
            updatedConfig.subProjects.forEachIndexed { index, project ->
                val projectText = if (project.path == project.name) {
                    project.name
                } else {
                    "${project.name}:${project.path}"
                }
                println("  ${index + 1}. $projectText")
            }
            println()
            println("${AnsiColors.BLUE}Config file:${AnsiColors.RESET} ${parentConfig.filePath.absolute()}")
            0
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save parent configuration: ${e.message}")
            1
        }
    }
    
    private fun showUsage() {
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project specification is required")
        println()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra config add-subproject <project-name>")
        println("  kinfra config add-subproject <project-name>:<path>")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  kinfra config add-subproject project-a")
        println("  kinfra config add-subproject project-b:../project-b")
        println("  kinfra config add-subproject project-c:/opt/project-c")
        println()
        println("${AnsiColors.BLUE}Note:${AnsiColors.RESET}")
        println("  If path is not specified, the project name will be used as the path")
    }
}