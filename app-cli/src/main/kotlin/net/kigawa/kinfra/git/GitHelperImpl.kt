package net.kigawa.kinfra.git

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

/**
 * Git repository operations implementation
 */
class GitHelperImpl(
    private val configRepository: ConfigRepository
) : GitHelper {
    /**
     * Check if the specified directory is a git repository
     */
    override fun isGitRepository(workingDir: File): Boolean {
        val gitDir = File(workingDir, ".git")
        return gitDir.exists() && gitDir.isDirectory
    }

    /**
     * Get repository path from project.yaml
     */
    private fun getRepositoryPath(): File? {
        val projectConfig = configRepository.loadGlobalConfig()
        val repo = projectConfig.login?.repo
        return if (repo != null && repo.isNotEmpty()) {
            // repo contains "user/repo" format, need to convert to local path
            // Use FilePaths to get the base directory
            val repoDirName = repo.substringAfterLast('/')
            val userHome = System.getProperty("user.home")
            File("$userHome/.local/kinfra/repos/$repoDirName")
        } else {
            null
        }
    }

    /**
     * Execute git pull in the repository specified in project.yaml
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

    /**
     * Clone a git repository to the specified directory
     * @param repoUrl Git repository URL (e.g., https://github.com/user/repo.git or git@github.com:user/repo.git)
     * @param targetDir Target directory to clone into
     * @return true if clone was successful, false if failed
     */
    override fun cloneRepository(repoUrl: String, targetDir: File): Boolean {
        if (targetDir.exists()) {
            if (targetDir.listFiles()?.isNotEmpty() == true) {
                println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Target directory is not empty: ${targetDir.absolutePath}")
                print("Continue and overwrite? (y/N): ")
                val confirm = readlnOrNull()?.lowercase()
                if (confirm != "y" && confirm != "yes") {
                    println("${AnsiColors.BLUE}Clone cancelled${AnsiColors.RESET}")
                    return false
                }
            }
        } else {
            // Create parent directories if they don't exist
            if (!targetDir.mkdirs()) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create target directory: ${targetDir.absolutePath}")
                return false
            }
        }

        return try {
            println("${AnsiColors.BLUE}Cloning repository from $repoUrl...${AnsiColors.RESET}")
            val process = ProcessBuilder("git", "clone", repoUrl, targetDir.absolutePath)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            val errorOutput = process.errorStream.bufferedReader().readText()

            if (exitCode == 0) {
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Successfully cloned repository to ${targetDir.absolutePath}")
                if (output.isNotBlank()) {
                    println(output.trim())
                }
                true
            } else {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to clone repository")
                if (errorOutput.isNotBlank()) {
                    println(errorOutput.trim())
                }
                if (output.isNotBlank()) {
                    println(output.trim())
                }
                false
            }
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to execute git clone: ${e.message}")
            false
        }
    }
}
