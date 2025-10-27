package net.kigawa.kinfra.infrastructure.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.err.Res
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.logging.Logger
import java.io.File

class SubmoduleAction(
    private val processExecutor: ProcessExecutor,
    private val logger: Logger
) : Action {
    override fun execute(args: List<String>): Int {
        if (args.isNotEmpty()) {
            println("${AnsiColors.RED}Error: 'submodule' command does not accept arguments${AnsiColors.RESET}")
            return 1
        }

        try {
            val workingDir = File(System.getProperty("user.dir"))
            logger.info("Initializing and updating submodules in: $workingDir")

            // Update submodules
            val result = processExecutor.execute(arrayOf("git", "submodule", "update", "--init", "--recursive"), workingDir)
            when (result) {
                is Res.Err -> {
                    println("${AnsiColors.RED}Error: Failed to update submodules${AnsiColors.RESET}")
                    return result.err.exitCode
                }
                is Res.Ok -> {
                    println("${AnsiColors.GREEN}✓ Successfully updated submodules${AnsiColors.RESET}")
                    return 0
                }
            }
        } catch (e: Exception) {
            logger.error("Error executing submodule command", e)
            println("${AnsiColors.RED}Error: ${e.message}${AnsiColors.RESET}")
            return 1
        }
    }

    override fun getDescription(): String {
        return "Initialize and update Git submodules"
    }

    override fun showHelp() {
        super.showHelp()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra submodule")
        println()
        println("${AnsiColors.BLUE}Description:${AnsiColors.RESET}")
        println("  Initializes and updates all Git submodules in the current directory.")
        println("  This is equivalent to running 'git submodule update --init --recursive'.")
    }
}