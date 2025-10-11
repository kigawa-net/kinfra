package net.kigawa.kinfra.util

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

    /**
     * Apply color to text and automatically reset
     */
    fun red(text: String) = "$RED$text$RESET"
    fun green(text: String) = "$GREEN$text$RESET"
    fun yellow(text: String) = "$YELLOW$text$RESET"
    fun blue(text: String) = "$BLUE$text$RESET"
    fun cyan(text: String) = "$CYAN$text$RESET"
    fun bold(text: String) = "$BOLD$text$RESET"
}