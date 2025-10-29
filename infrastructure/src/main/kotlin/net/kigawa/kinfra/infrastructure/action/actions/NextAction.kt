package net.kigawa.kinfra.infrastructure.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kodel.err.Res
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.logging.Logger
import java.io.File
import kotlin.io.path.Path

class NextAction(
    private val processExecutor: ProcessExecutor,
    private val loginRepo: LoginRepo,
    private val logger: Logger
) : Action {
    override fun execute(args: List<String>): Int {
        if (args.isNotEmpty()) {
            println("${AnsiColors.RED}Error: 'next' command does not accept arguments${AnsiColors.RESET}")
            return 1
        }

        try {
            // Get the logged-in repository path
            val repoPath = loginRepo.repoPath.toFile()
            logger.info("Pulling latest changes for repository at: $repoPath")

            // Check if there are local changes
            val statusResult = processExecutor.executeWithOutput(arrayOf("git", "status", "--porcelain"), repoPath)
            val hasLocalChanges = statusResult.exitCode == 0 && statusResult.output.isNotBlank()

            var stashed = false
            if (hasLocalChanges) {
                logger.info("Local changes detected, stashing them")
                println("${AnsiColors.YELLOW}⚠ Local changes detected, temporarily stashing...${AnsiColors.RESET}")

                // Stash local changes
                val stashResult = processExecutor.execute(arrayOf("git", "stash", "push", "-m", "kinfra-next-auto-stash"), repoPath)
                when (stashResult) {
                    is Res.Err -> {
                        println("${AnsiColors.RED}Error: Failed to stash local changes${AnsiColors.RESET}")
                        return stashResult.err.exitCode
                    }
                    is Res.Ok -> {
                        stashed = true
                        println("${AnsiColors.GREEN}✓ Local changes stashed${AnsiColors.RESET}")
                    }
                }
            }

            // Pull the latest changes
            val pullResult = processExecutor.execute(arrayOf("git", "pull", "--no-rebase"), repoPath)
            when (pullResult) {
                is Res.Err -> {
                    println("${AnsiColors.RED}Error: Failed to pull repository${AnsiColors.RESET}")
                    // If we stashed, try to restore
                    if (stashed) {
                        val popResult = processExecutor.execute(arrayOf("git", "stash", "pop"), repoPath)
                        when (popResult) {
                            is Res.Err -> {
                                println("${AnsiColors.YELLOW}⚠ Failed to restore stashed changes due to conflicts${AnsiColors.RESET}")
                                println("${AnsiColors.BLUE}The stash entry is kept in case you need it again.${AnsiColors.RESET}")
                                println("${AnsiColors.BLUE}Please resolve conflicts manually and run 'git stash drop' when done.${AnsiColors.RESET}")
                            }
                            is Res.Ok -> {
                                println("${AnsiColors.GREEN}✓ Stashed changes restored${AnsiColors.RESET}")
                            }
                        }
                    }
                    return pullResult.err.exitCode
                }
                is Res.Ok -> {
                    // Success
                }
            }

            // Restore stashed changes if any
            if (stashed) {
                logger.info("Restoring stashed changes")
                val popResult = processExecutor.execute(arrayOf("git", "stash", "pop"), repoPath)
                when (popResult) {
                    is Res.Err -> {
                        println("${AnsiColors.YELLOW}⚠ Failed to restore stashed changes due to conflicts${AnsiColors.RESET}")
                        println("${AnsiColors.BLUE}The stash entry is kept in case you need it again.${AnsiColors.RESET}")
                        println("${AnsiColors.BLUE}Please resolve conflicts manually and run 'git stash drop' when done.${AnsiColors.RESET}")
                        // Don't return error, continue with the rest of the process
                    }
                    is Res.Ok -> {
                        println("${AnsiColors.GREEN}✓ Stashed changes restored${AnsiColors.RESET}")
                    }
                }
            }

            println("${AnsiColors.GREEN}✓ Successfully pulled latest changes${AnsiColors.RESET}")

            // Update submodules
            logger.info("Updating submodules")
            val submoduleResult = processExecutor.execute(arrayOf("git", "submodule", "update", "--init", "--recursive"), repoPath)
            when (submoduleResult) {
                is Res.Err -> {
                    println("${AnsiColors.RED}Error: Failed to update submodules${AnsiColors.RESET}")
                    return submoduleResult.err.exitCode
                }
                is Res.Ok -> {
                    // Success
                }
            }

            println("${AnsiColors.GREEN}✓ Successfully updated submodules${AnsiColors.RESET}")

            // Detect and enable updated sub-projects
            val enableResult = enableUpdatedSubProjects(repoPath)
            if (enableResult != 0) {
                return enableResult
            }

            return 0
        } catch (e: Exception) {
            logger.error("Error executing next command", e)
            println("${AnsiColors.RED}Error: ${e.message}${AnsiColors.RESET}")
            return 1
        }
    }

    override fun getDescription(): String {
        return "Pull latest changes and update submodules for the logged-in repository"
    }

    private fun enableUpdatedSubProjects(repoPath: File): Int {
        try {
            logger.info("Detecting changed sub-projects")

            // Get submodule status to find changed submodules
            val submoduleStatusResult = processExecutor.executeWithOutput(arrayOf("git", "submodule", "status"), repoPath)
            if (submoduleStatusResult.exitCode != 0) {
                logger.warn("Failed to get submodule status: ${submoduleStatusResult.error}")
                return 0 // Not a fatal error, continue
            }

            // Parse submodule status to find changed submodules
            val changedSubmodulePaths = parseChangedSubmodules(submoduleStatusResult.output.lines())

            if (changedSubmodulePaths.isEmpty()) {
                logger.info("No changed sub-projects detected")
                return 0
            }

            // Load parent config
            val parentConfig = loginRepo.loadKinfraBaseConfig()
            if (parentConfig == null) {
                logger.warn("No parent configuration found, skipping sub-project enablement")
                return 0
            }

            // Find sub-projects that have changes and exist
            val changedSubProjects = mutableListOf<net.kigawa.kinfra.model.sub.SubProject>()

            for (subProject in parentConfig.subProjects) {
                if (subProject.path in changedSubmodulePaths) {
                    val subProjectPath = Path(repoPath.absolutePath, subProject.path).toFile()
                    if (subProjectPath.exists() && subProjectPath.isDirectory) {
                        // Check if it's a git repository
                        val gitDir = Path(subProjectPath.absolutePath, ".git").toFile()
                        if (gitDir.exists()) {
                            changedSubProjects.add(subProject)
                        }
                    }
                }
            }

            if (changedSubProjects.isEmpty()) {
                logger.info("No configured sub-projects have changes")
                return 0
            }

            // Keep only changed sub-projects in config
            val currentData = parentConfig.toData()
            val updatedData = currentData.copy(subProjects = changedSubProjects)

            parentConfig.saveData(updatedData)

            println("${AnsiColors.GREEN}✓ Enabled ${changedSubProjects.size} changed sub-project(s):${AnsiColors.RESET}")
            changedSubProjects.forEach { subProject ->
                println("  - ${subProject.name}: ${subProject.path}")
            }

            return 0
        } catch (e: Exception) {
            logger.error("Error enabling changed sub-projects", e)
            println("${AnsiColors.RED}Error: Failed to enable changed sub-projects: ${e.message}${AnsiColors.RESET}")
            return 1
        }
    }

    private fun parseChangedSubmodules(submoduleLines: List<String>): List<String> {
        val changedSubmodulePaths = mutableListOf<String>()

        for (line in submoduleLines) {
            if (line.isBlank()) continue

            try {
                // Parse submodule status line
                // Format: [status][commit-hash] path (description)
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) continue

                // Check if the line starts with a status indicator
                if (trimmedLine.startsWith("+") || trimmedLine.startsWith("-") || trimmedLine.startsWith("U")) {
                    // Extract path (after commit hash, before description)
                    val afterStatus = trimmedLine.substring(1).trim()
                    if (afterStatus.matches(Regex("[0-9a-f]{40}.*"))) {
                        val pathPart = afterStatus.substring(40).trim().substringBefore('(').trim()
                        if (pathPart.isNotEmpty()) {
                            changedSubmodulePaths.add(pathPart)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to parse submodule line: $line - ${e.message}")
            }
        }

        return changedSubmodulePaths
    }

    override fun showHelp() {
        super.showHelp()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra next")
        println()
        println("${AnsiColors.BLUE}Description:${AnsiColors.RESET}")
        println("  Pulls the latest changes from the remote repository and updates all submodules")
        println("  for the currently logged-in repository.")
        println("  Additionally detects sub-projects that have changes and enables only those")
        println("  in the parent configuration (kinfra-parent.yaml).")
    }
}