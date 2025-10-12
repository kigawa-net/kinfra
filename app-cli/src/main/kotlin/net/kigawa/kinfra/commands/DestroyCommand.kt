package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.AnsiColors

class DestroyCommand(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper
) : Command {
    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        val result = terraformService.destroy(args)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Destroy the Terraform-managed infrastructure"
    }
}