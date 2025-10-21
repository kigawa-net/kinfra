package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.execution.ConfigEditor
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors

class ConfigEditAction(
    private val loginRepo: LoginRepo,
    val logger: Logger,
): Action {
    
    private val configEditor = ConfigEditor(loginRepo)

    override fun execute(args: List<String>): Int {
        // Check for add-subproject subcommand
        if (args.isNotEmpty() && args[0] == "add-subproject") {
            logger.debug("add-subproject subcommand found: $args")
            return configEditor.addSubProject(args.drop(1).toTypedArray())
        }

        val isParentConfig = args.contains("--parent") || args.contains("-p")

        return if (isParentConfig) {
            configEditor.editParentConfig()
        } else {
            configEditor.editProjectConfig()
        }
    }

    override fun getDescription(): String {
        return "Edit kinfra configuration files or manage parent project (use --parent to edit parent config, add-subproject to add sub-projects)"
    }

    override fun showHelp() {
        println("${AnsiColors.BLUE}Description:${AnsiColors.RESET}")
        println("  Edit kinfra configuration files or manage parent project")
        println()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra config [options]")
        println("  kinfra config add-subproject <project-name>")
        println()
        println("${AnsiColors.BLUE}Options:${AnsiColors.RESET}")
        println("  --parent, -p   Edit parent project configuration (kinfra-parent.yaml)")
        println()
        println("${AnsiColors.BLUE}Subcommands:${AnsiColors.RESET}")
        println("  add-subproject <name[:path]>   Add a sub-project to parent configuration")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  kinfra config                      Edit project configuration")
        println("  kinfra config --parent             Edit parent configuration")
        println("  kinfra config add-subproject app1  Add 'app1' to parent config")
        println("  kinfra config add-subproject app2:../app2  Add 'app2' with custom path")
        println()
    }
}