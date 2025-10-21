package net.kigawa.kinfra.action.execution

import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors
import java.nio.file.Path

/**
 * Utility class to retrieve configuration paths and handle missing login configuration.
 */
object ConfigUtils {
    /**
     * Retrieves the project configuration path or prints an error if login configuration is missing.
     * @return the Path to the configuration file, or null if login configuration is absent.
     */
    fun getProjectConfigPath(loginRepo: LoginRepo): Path? {
        return try {
            loginRepo.kinfraConfigPath()
        } catch (_: IllegalStateException) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Login configuration not found")
            println("${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra login <github-repo>' first")
            null
        }
    }
}
