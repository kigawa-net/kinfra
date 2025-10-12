package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.AnsiColors

class ValidateCommand(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper
) : Command {
    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        val result = terraformService.validate()
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Validate the configuration files"
    }
}
