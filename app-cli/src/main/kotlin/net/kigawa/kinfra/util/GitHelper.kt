package net.kigawa.kinfra.util

import java.io.File

object GitHelper {
    /**
     * Check if the current directory is a git repository
     */
    fun isGitRepository(workingDir: File = File(System.getProperty("user.dir"))): Boolean {
        val gitDir = File(workingDir, ".git")
        return gitDir.exists() && gitDir.isDirectory
    }

    /**
     * Execute git pull in the current repository
     * @return true if pull was successful, false otherwise
     */
    fun pullRepository(workingDir: File = File(System.getProperty("user.dir"))): Boolean {
        if (!isGitRepository(workingDir)) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Not a git repository, skipping git pull")
            return true // Not an error, just skip
        }

        try {
            println("${AnsiColors.BLUE}Pulling latest changes from git repository...${AnsiColors.RESET}")
            val process = ProcessBuilder("git", "pull")
                .directory(workingDir)
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
                return true
            } else {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to pull from git repository")
                if (errorOutput.isNotBlank()) {
                    println(errorOutput.trim())
                }
                if (output.isNotBlank()) {
                    println(output.trim())
                }
                return false
            }
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to execute git pull: ${e.message}")
            return false
        }
    }
}
