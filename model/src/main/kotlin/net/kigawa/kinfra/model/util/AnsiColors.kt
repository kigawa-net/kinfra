package net.kigawa.kinfra.model.util

import net.kigawa.kinfra.model.LogCategory

/**
 * ANSI color codes for terminal output
 */
object AnsiColors {
    const val RESET = "\u001B[0m"
    const val BOLD = "\u001B[1m"

    // Regular colors
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val CYAN = "\u001B[36m"
    const val MAGENTA = "\u001B[35m"
    const val WHITE = "\u001B[37m"

    /**
     * Apply color to text and automatically reset
     */
    fun red(text: String) = "$RED$text$RESET"
    fun green(text: String) = "$GREEN$text$RESET"
    fun yellow(text: String) = "$YELLOW$text$RESET"
    fun blue(text: String) = "$BLUE$text$RESET"
    fun cyan(text: String) = "$CYAN$text$RESET"
    fun magenta(text: String) = "$MAGENTA$text$RESET"
    fun bold(text: String) = "$BOLD$text$RESET"

    /**
     * Apply color based on log category
     */
    fun colorize(text: String, category: LogCategory): String {
        return when (category) {
            LogCategory.INFO -> cyan(text)
            LogCategory.SUCCESS -> green(text)
            LogCategory.WARNING -> yellow(text)
            LogCategory.ERROR -> red(text)
            LogCategory.COMMAND -> blue(text)
            LogCategory.DEBUG -> magenta(text)
        }
    }

    /**
     * Get the color code for a category
     */
    fun getColorForCategory(category: LogCategory): String {
        return when (category) {
            LogCategory.INFO -> CYAN
            LogCategory.SUCCESS -> GREEN
            LogCategory.WARNING -> YELLOW
            LogCategory.ERROR -> RED
            LogCategory.COMMAND -> BLUE
            LogCategory.DEBUG -> MAGENTA
        }
    }
}
