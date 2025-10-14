package net.kigawa.kinfra.action.actions
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isSuccess
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors

class PlanAction(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper
) : Action {
    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        val result = terraformService.plan(args)
        return result.exitCode()
    }

    override fun getDescription(): String {
        return "Create an execution plan"
    }
}
