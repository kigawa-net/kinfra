package net.kigawa.kinfra.model

import net.kigawa.kinfra.model.util.AnsiColors

interface Action {
    fun execute(args: Array<String>): Int
    fun getDescription(): String

    /**
     * Show detailed help for this action
     * Default implementation shows the description only
     */
    fun showHelp() {
        println("${AnsiColors.BLUE}Description:${AnsiColors.RESET}")
        println("  ${getDescription()}")
        println()
    }
}
