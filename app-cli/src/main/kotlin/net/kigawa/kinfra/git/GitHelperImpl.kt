package net.kigawa.kinfra.git

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.util.AnsiColors
import java.io.File

/**
 * Git repository operations implementation
 */
class GitHelperImpl : GitHelper {
    private val configDir = "${System.getProperty("user.home")}/.local/kinfra"
    private val projectConfigFile = File(configDir, "project.json")

    /**
     * Check if the specified directory is a git repository
     */
    override fun isGitRepository(workingDir: File): Boolean {
        val gitDir = File(workingDir, ".git")
        return gitDir.exists() && gitDir.isDirectory
    }

    /**
     * Get repository path from ~/.local/kinfra/project.json
     */
    private fun getRepositoryPath(): File? {
        if (!projectConfigFile.exists()) {
            return null
        }

        return try {
            val json = projectConfigFile.readText()
            // Simple JSON parsing for githubRepository field
            val githubRepoPattern = """"githubRepository"\s*:\s*"([^"]+)"""".toRegex()
            val match = githubRepoPattern.find(json)
            val repoPath = match?.groupValues?.get(1)

            if (repoPath != null && repoPath.isNotEmpty()) {
                File(repoPath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Execute git pull in the repository specified in ~/.local/kinfra/project.json
     * @return true if pull was successful or skipped (not an error), false if failed
     */
    override fun pullRepository(): Boolean {
        val repoDir = getRepositoryPath()

        if (repoDir == null) {
            // No repository configured, skip silently
            return true
        }

        if (!repoDir.exists()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Configured repository path does not exist: ${repoDir.absolutePath}")
            return true // Not a fatal error
        }

        if (!isGitRepository(repoDir)) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Configured path is not a git repository: ${repoDir.absolutePath}")
            return true // Not a fatal error
        }

        return try {
            println("${AnsiColors.BLUE}Pulling latest changes from git repository...${AnsiColors.RESET}")
            val process = ProcessBuilder("git", "pull")
                .directory(repoDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            val errorOutput = process.errorStream.bufferedReader().readText()

            if (exitCode == 0) {
                if (output.contains("Already up to date") || output.contains("Already up-to-date")) {
                    println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Repository is up to date")
                } else {
                    println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Successfully pulled changes")
                    if (output.isNotBlank()) {
                        println(output.trim())
                    }
                }
                true
            } else {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to pull from git repository")
                if (errorOutput.isNotBlank()) {
                    println(errorOutput.trim())
                }
                if (output.isNotBlank()) {
                    println(output.trim())
                }
                false
            }
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to execute git pull: ${e.message}")
            false
        }
    }
}
