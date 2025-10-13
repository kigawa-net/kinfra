package net.kigawa.kinfra.actions

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors

class HelpAction(
    private val commands: Map<String, Action>,
    private val gitHelper: GitHelper
) : Action {
    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

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

}
