package net.kigawa.kinfra.commands

import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.AnsiColors

abstract class EnvironmentCommand : Command {
    override fun requiresEnvironment(): Boolean = true

    protected fun printInfo(message: String) {
        println("${AnsiColors.BLUE}$message${AnsiColors.RESET}")
    }
}
