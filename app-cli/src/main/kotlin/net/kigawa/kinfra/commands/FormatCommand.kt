package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.AnsiColors
import net.kigawa.kinfra.util.GitHelper

class FormatCommand(
    private val terraformService: TerraformService
) : Command {
    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!GitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        val result = terraformService.format(recursive = true)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Reformat configuration files to canonical format"
    }
}
