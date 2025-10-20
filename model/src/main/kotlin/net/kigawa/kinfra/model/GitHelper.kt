package net.kigawa.kinfra.model

import java.io.File

/**
 * Git repository operations helper
 */
interface GitHelper {
    /**
     * Check if the specified directory is a git repository
     */
    fun isGitRepository(workingDir: File): Boolean

    /**
     * Execute git pull in the repository specified in the project configuration
     * @return true if pull was successful or skipped (not an error), false if failed
     */
    fun pullRepository(): Boolean

    /**
     * Clone a git repository to the specified directory
     * @param repoUrl Git repository URL (e.g., https://github.com/user/repo.git or git@github.com:user/repo.git)
     * @param targetDir Target directory to clone into
     * @return true if clone was successful, false if failed
     */
    fun cloneRepository(repoUrl: String, targetDir: File): Boolean

    /**
     * Get the configured repository directory
     * @return Repository directory or null if not configured
     */
    fun getRepositoryDirectory(): File?

    /**
     * Get git status for the configured repository
     * @return Pair of (exitCode, output) or null if no repository configured
     */
    fun getStatus(): Pair<Int, String>?

     /**
      * Add all changes to staging area
      * @return true if add was successful, false if failed
      */
     fun addChanges(): Boolean

     /**
      * Commit staged changes with a message
      * @param message Commit message
      * @return true if commit was successful, false if failed
      */
     fun commitChanges(message: String): Boolean

     /**
      * Push changes to remote repository
      * @return true if push was successful, false if failed
      */
     fun pushToRemote(): Boolean
}
