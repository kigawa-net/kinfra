package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.message
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.isFailure

class ApplyAction(
    private val terraformService: TerraformService
) : Action {
    override fun execute(args: List<String>): Int {
        // Terraform設定が取得できない場合は実行しない
        val config = terraformService.getTerraformConfig()
        if (config == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Terraform configuration not found. Please check your kinfra.yaml file.")
            return 1
        }

        // Check if first arg is a plan file
        val planFile = if (args.isNotEmpty() &&
            (args[0].endsWith(".tfplan") || args[0] == "tfplan")) {
            args[0]
        } else {
            null
        }

        val argsWithoutPlan = if (planFile != null) args.drop(1) else args

        val result = terraformService.apply(planFile, argsWithoutPlan, quiet = false)

        // エラーが発生した場合、プロジェクト情報を表示
        if (result.isFailure()) {
            println("${AnsiColors.RED}Error in project:${AnsiColors.RESET} ${config.workingDirectory.absolutePath}")
            result.message()?.let { println("${AnsiColors.RED}Details: $it${AnsiColors.RESET}") }
        }

        return result.exitCode()
    }

    override fun getDescription(): String {
        return "Apply the changes required to reach the desired state"
    }
}
