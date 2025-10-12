package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.AnsiColors
import net.kigawa.kinfra.util.GitHelper

class InitCommand(
    private val terraformService: TerraformService
) : Command {
    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!GitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        val config = terraformService.getTerraformConfig()
        println("${AnsiColors.BLUE}Working directory:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")

        val result = terraformService.init(args)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Initialize Terraform working directory"
    }
}