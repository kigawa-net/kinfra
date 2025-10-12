package net.kigawa.kinfra.action

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
     * Execute git pull in the repository specified in ~/.local/kinfra/project.json
     * @return true if pull was successful or skipped (not an error), false if failed
     */
    fun pullRepository(): Boolean
}
