package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.util.ColorLogger

class InitCommand(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper
) : Command {
    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            ColorLogger.warning("Warning: Failed to pull from git repository, continuing anyway...")
        }

        val config = terraformService.getTerraformConfig()
        ColorLogger.info("Working directory: ${config.workingDirectory.absolutePath}")

        val result = terraformService.init(args)
        return result.exitCode
    }

    override fun getDescription(): String {
        return "Initialize Terraform working directory"
    }
}