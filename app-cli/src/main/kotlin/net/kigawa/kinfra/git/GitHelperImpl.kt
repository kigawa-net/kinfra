package net.kigawa.kinfra.git

import net.kigawa.kinfra.model.GitHelper
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

    /**
     * Get the configured repository directory
     * @return Repository directory or null if not configured
     */
    override fun getRepositoryDirectory(): File? {
        return getRepositoryPath()
    }

    /**
     * Get git status for the configured repository
     * @return Pair of (exitCode, output) or null if no repository configured
     */
    override fun getStatus(): Pair<Int, String>? {
        val repoDir = getRepositoryPath() ?: return null

        if (!repoDir.exists() || !isGitRepository(repoDir)) {
            return null
        }

        return try {
            val process = ProcessBuilder("git", "status")
                .directory(repoDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            val errorOutput = process.errorStream.bufferedReader().readText()

            Pair(exitCode, if (exitCode == 0) output else errorOutput)
        } catch (e: Exception) {
            Pair(1, "Error: ${e.message}")
        }
    }

     /**
      * Add all changes to staging area
      * @return true if add was successful, false if failed
      */
     override fun addChanges(): Boolean {
         val repoDir = getRepositoryPath() ?: return false

         if (!repoDir.exists() || !isGitRepository(repoDir)) {
             println("${AnsiColors.RED}Error:${AnsiColors.RESET} Repository not found or not a git repository: ${repoDir.absolutePath}")
             return false
         }

         return try {
             println("${AnsiColors.BLUE}Adding all changes...${AnsiColors.RESET}")
             val process = ProcessBuilder("git", "add", ".")
                 .directory(repoDir)
                 .redirectOutput(ProcessBuilder.Redirect.PIPE)
                 .redirectError(ProcessBuilder.Redirect.PIPE)
                 .start()

             val exitCode = process.waitFor()
             val output = process.inputStream.bufferedReader().readText()
             val errorOutput = process.errorStream.bufferedReader().readText()

             if (exitCode == 0) {
                 println("${AnsiColors.GREEN}✓ Successfully added changes${AnsiColors.RESET}")
                 true
             } else {
                 println("${AnsiColors.RED}Error adding changes:${AnsiColors.RESET}")
                 if (errorOutput.isNotBlank()) {
                     println(errorOutput.trim())
                 }
                 false
             }
         } catch (e: Exception) {
             println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to execute git add: ${e.message}")
             false
         }
     }

     /**
      * Commit staged changes with a message
      * @param message Commit message
      * @return true if commit was successful, false if failed
      */
     override fun commitChanges(message: String): Boolean {
         val repoDir = getRepositoryPath() ?: return false

         if (!repoDir.exists() || !isGitRepository(repoDir)) {
             println("${AnsiColors.RED}Error:${AnsiColors.RESET} Repository not found or not a git repository: ${repoDir.absolutePath}")
             return false
         }

         return try {
             println("${AnsiColors.BLUE}Committing changes with message: $message${AnsiColors.RESET}")
             val process = ProcessBuilder("git", "commit", "-m", message)
                 .directory(repoDir)
                 .redirectOutput(ProcessBuilder.Redirect.PIPE)
                 .redirectError(ProcessBuilder.Redirect.PIPE)
                 .start()

             val exitCode = process.waitFor()
             val output = process.inputStream.bufferedReader().readText()
             val errorOutput = process.errorStream.bufferedReader().readText()

             if (exitCode == 0) {
                 println("${AnsiColors.GREEN}✓ Successfully committed changes${AnsiColors.RESET}")
                 if (output.isNotBlank()) {
                     println(output.trim())
                 }
                 true
             } else if (errorOutput.contains("nothing to commit")) {
                 println("${AnsiColors.YELLOW}Nothing to commit${AnsiColors.RESET}")
                 true // Not an error
             } else {
                 println("${AnsiColors.RED}Error committing changes:${AnsiColors.RESET}")
                 if (errorOutput.isNotBlank()) {
                     println(errorOutput.trim())
                 }
                 false
             }
         } catch (e: Exception) {
             println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to execute git commit: ${e.message}")
             false
         }
     }

     /**
      * Push changes to remote repository
      * @return true if push was successful, false if failed
      */
     override fun pushToRemote(): Boolean {
        val repoDir = getRepositoryPath()

        if (repoDir == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} No repository configured")
            return false
        }

        if (!repoDir.exists() || !isGitRepository(repoDir)) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Repository not found or not a git repository: ${repoDir.absolutePath}")
            return false
        }

        return try {
            // Get current branch
            val branchProcess = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
                .directory(repoDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val branchExitCode = branchProcess.waitFor()
            val branchOutput = branchProcess.inputStream.bufferedReader().readText().trim()
            val branchError = branchProcess.errorStream.bufferedReader().readText()

            if (branchExitCode != 0) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Could not determine current branch")
                println("${AnsiColors.RED}Details:${AnsiColors.RESET} $branchError")
                return false
            }

            if (branchOutput.isEmpty()) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Branch name is empty")
                return false
            }

            val currentBranch = branchOutput
            println("${AnsiColors.BLUE}Current branch: ${AnsiColors.CYAN}$currentBranch${AnsiColors.RESET}")
            println()

            // Show status
            val statusProcess = ProcessBuilder("git", "status", "--short")
                .directory(repoDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            statusProcess.waitFor()
            val statusOutput = statusProcess.inputStream.bufferedReader().readText()

            if (statusOutput.isNotEmpty()) {
                println("${AnsiColors.YELLOW}Uncommitted changes:${AnsiColors.RESET}")
                println(statusOutput)
                println()
            }

            // Prompt for confirmation
            print("${AnsiColors.GREEN}Do you want to push to origin/$currentBranch? (yes/no):${AnsiColors.RESET} ")
            val confirmation = readlnOrNull()?.trim()?.lowercase() ?: ""

            if (confirmation != "yes" && confirmation != "y") {
                println("${AnsiColors.YELLOW}Push cancelled.${AnsiColors.RESET}")
                return false
            }

            println()
            println("${AnsiColors.BLUE}Pushing to origin/$currentBranch...${AnsiColors.RESET}")

            // Execute push
            val pushProcess = ProcessBuilder("git", "push", "origin", currentBranch)
                .directory(repoDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val pushExitCode = pushProcess.waitFor()
            val pushOutput = pushProcess.inputStream.bufferedReader().readText()
            val pushError = pushProcess.errorStream.bufferedReader().readText()

            if (pushExitCode == 0) {
                println("${AnsiColors.GREEN}✓ Successfully pushed to origin/$currentBranch${AnsiColors.RESET}")
                if (pushOutput.isNotEmpty()) {
                    println(pushOutput)
                }
                true
            } else {
                println("${AnsiColors.RED}Error pushing to repository:${AnsiColors.RESET}")
                println(pushError)
                false
            }
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to execute git push: ${e.message}")
            false
        }
    }
}
