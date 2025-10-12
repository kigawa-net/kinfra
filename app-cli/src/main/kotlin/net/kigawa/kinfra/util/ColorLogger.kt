package net.kigawa.kinfra.util

import net.kigawa.kinfra.model.LogCategory

/**
 * Console logger with category-based color coding for CLI output
 */
object ColorLogger {
    /**
     * Log a message with the specified category
     */
    fun log(message: String, category: LogCategory = LogCategory.INFO) {
        println(AnsiColors.colorize(message, category))
    }

    /**
     * Log an info message (cyan)
     */
    fun info(message: String) {
        log(message, LogCategory.INFO)
    }

    /**
     * Log a success message (green)
     */
    fun success(message: String) {
        log(message, LogCategory.SUCCESS)
    }

    /**
     * Log a warning message (yellow)
     */
    fun warning(message: String) {
        log(message, LogCategory.WARNING)
    }

    /**
     * Log an error message (red)
     */
    fun error(message: String) {
        log(message, LogCategory.ERROR)
    }

    /**
     * Log a command execution message (blue)
     */
    fun command(message: String) {
        log(message, LogCategory.COMMAND)
    }

    /**
     * Log a debug message (magenta)
     */
    fun debug(message: String) {
        log(message, LogCategory.DEBUG)
    }
}
