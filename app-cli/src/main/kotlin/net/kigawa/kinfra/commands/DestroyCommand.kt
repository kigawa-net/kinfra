package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command

class DestroyCommand(
    private val terraformService: TerraformService
) : Command {
    override fun execute(args: Array<String>): Int {
        val result = terraformService.destroy(args)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Destroy the Terraform-managed infrastructure"
    }
}