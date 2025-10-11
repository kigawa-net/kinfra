package net.kigawa.kinfra.commands

import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.AnsiColors

class HelpCommand(private val commands: Map<String, Command>) : Command {
    override fun execute(args: Array<String>): Int {
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET} java -jar app.jar [command] [options]")
        println()
        println("${AnsiColors.BLUE}Commands:${AnsiColors.RESET}")

        commands.forEach { (name, command) ->
            val padding = " ".repeat(maxOf(0, 10 - name.length))
            println("  $name$padding - ${command.getDescription()}")
        }

        println()
        println("${AnsiColors.BLUE}Environment:${AnsiColors.RESET}")
        println("  prod       - Production environment (automatically selected)")
        println()
        println("${AnsiColors.BLUE}Options:${AnsiColors.RESET}")
        println("  -auto-approve  - Skip interactive approval (for apply/destroy)")
        println("  -var-file      - Specify a variable file")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  java -jar app.jar init")
        println("  java -jar app.jar plan")
        println("  java -jar app.jar apply")
        println("  java -jar app.jar deploy")
        println("  java -jar app.jar deploy -auto-approve")
        println("  java -jar app.jar destroy -auto-approve")
        println("  java -jar app.jar fmt")
        println("  java -jar app.jar validate")

        return 0
    }

    override fun getDescription(): String {
        return "Show this help message"
    }

    override fun requiresEnvironment(): Boolean {
        return false
    }
}
