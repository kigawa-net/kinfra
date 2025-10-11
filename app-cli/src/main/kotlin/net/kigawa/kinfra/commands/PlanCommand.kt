package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command

class PlanCommand(
    private val terraformService: TerraformService
) : Command {
    override fun execute(args: Array<String>): Int {
        val result = terraformService.plan(args)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Create an execution plan"
    }
}