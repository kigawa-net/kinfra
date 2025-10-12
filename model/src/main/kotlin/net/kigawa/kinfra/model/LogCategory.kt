package net.kigawa.kinfra.model

/**
 * Log message categories for color-coded output
 */
enum class LogCategory {
    /** General information messages */
    INFO,
    /** Success messages */
    SUCCESS,
    /** Warning messages */
    WARNING,
    /** Error messages */
    ERROR,
    /** Command execution output */
    COMMAND,
    /** Debug messages */
    DEBUG
}
