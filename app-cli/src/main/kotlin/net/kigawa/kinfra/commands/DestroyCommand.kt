package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.EnvironmentValidator
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.util.AnsiColors

class DestroyCommand(
    private val terraformService: TerraformService,
    private val environmentValidator: EnvironmentValidator
) : EnvironmentCommand() {
    override fun execute(args: Array<String>): Int {
        if (args.isEmpty()) return 1

        val environmentName = args[0]
        val isAutoSelected = args.contains("--auto-selected")
        val additionalArgs = args.drop(1).filter { it != "--auto-selected" }.toTypedArray()

        val environment = environmentValidator.validate(environmentName)
        if (environment == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Only 'prod' environment is allowed.")
            println("${AnsiColors.BLUE}Available environment:${AnsiColors.RESET} prod")
            return 1
        }

        if (isAutoSelected) {
            println("${AnsiColors.BLUE}Using environment:${AnsiColors.RESET} ${environment.name} (automatically selected)")
        }

        val result = terraformService.destroy(environment, additionalArgs)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Destroy the Terraform-managed infrastructure"
    }
}