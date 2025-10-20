package net.kigawa.kinfra.action.actions
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.message

import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.ColorLogger
import net.kigawa.kinfra.model.util.isFailure

class InitAction(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper
) : Action {
    override fun execute(args: List<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            ColorLogger.warning("Warning: Failed to pull from git repository, continuing anyway...")
        }

        val config = terraformService.getTerraformConfig()
        if (config == null) {
            ColorLogger.error("Terraform configuration not found. Please check your kinfra.yaml file.")
            return 1
        }

        ColorLogger.info("Working directory: ${config.workingDirectory.absolutePath}")

        val result = terraformService.init(args, quiet = false)

        // エラーが発生した場合、プロジェクト情報を表示
        if (result.isFailure()) {
            ColorLogger.error("Error in project: ${config.workingDirectory.absolutePath}")
            result.message()?.let { ColorLogger.error("Details: $it") }
        }

        return result.exitCode()
    }

    override fun getDescription(): String {
        return "Initialize Terraform working directory"
    }
}
