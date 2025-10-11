package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.AnsiColors

class InitCommand(
    private val terraformService: TerraformService
) : Command {
    override fun execute(args: Array<String>): Int {
        val config = terraformService.getTerraformConfig()
        println("${AnsiColors.BLUE}Working directory:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")

        val result = terraformService.init(args)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Initialize Terraform working directory"
    }
}