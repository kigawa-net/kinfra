package net.kigawa.kinfra.action.actions
import net.kigawa.kinfra.model.util.exitCode

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.isFailure

class DestroyAction(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper
) : Action {
    override fun execute(args: List<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        val result = terraformService.destroy(args, quiet = false)

        // エラーが発生した場合、プロジェクト情報を表示
        if (result.isFailure()) {
            val config = terraformService.getTerraformConfig()
            println("${AnsiColors.RED}Error in project:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")
        }

        return result.exitCode()
    }

    override fun getDescription(): String {
        return "Destroy the Terraform-managed infrastructure"
    }
}
