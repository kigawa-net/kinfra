package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.exitCode

class ApplyAction(
    private val terraformService: TerraformService
) : Action {
    override fun execute(args: List<String>): Int {
        // Check if first arg is a plan file
        val planFile = if (args.isNotEmpty() &&
            (args[0].endsWith(".tfplan") || args[0] == "tfplan")) {
            args[0]
        } else {
            null
        }

        val argsWithoutPlan = if (planFile != null) args.drop(1) else args

        val result = terraformService.apply(planFile, argsWithoutPlan, quiet = false)
        return result.exitCode()
    }

    override fun getDescription(): String {
        return "Apply the changes required to reach the desired state"
    }
}
