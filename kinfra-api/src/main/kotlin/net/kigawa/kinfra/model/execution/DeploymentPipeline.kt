package net.kigawa.kinfra.model.execution

import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message

/**
 * デプロイパイプラインの各ステップを担当するクラス
 */
class DeploymentPipeline(
    private val terraformService: TerraformService,
) {
    fun initializeTerraform(additionalArgs: List<String>): Int {
        println("${AnsiColors.BLUE}Calling terraformService.init${AnsiColors.RESET}")
        val result = terraformService.init(additionalArgs = additionalArgs)
        println("${AnsiColors.BLUE}terraformService.init returned: $result${AnsiColors.RESET}")
        return if (result.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (result.message()?.contains("Terraform configuration not found") == true) {
                0
            } else {
                println("${AnsiColors.RED}Terraform init failed: ${result.message()} (exit code: ${result.exitCode()})${AnsiColors.RESET}")
                result.exitCode()
            }
        } else {
            0
        }
    }

    fun createExecutionPlan(additionalArgs: List<String>): Int {
        println("${AnsiColors.BLUE}Calling terraformService.plan${AnsiColors.RESET}")
        val result = terraformService.plan(additionalArgs)
        println("${AnsiColors.BLUE}terraformService.plan returned: $result${AnsiColors.RESET}")
        return if (result.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (result.message()?.contains("Terraform configuration not found") == true) {
                0
            } else {
                println("${AnsiColors.RED}Terraform plan failed: ${result.message()} (exit code: ${result.exitCode()})${AnsiColors.RESET}")
                result.exitCode()
            }
        } else {
            0
        }
    }

    fun applyChanges(additionalArgs: List<String>): Int {
        val applyArgsWithAutoApprove =
            if (additionalArgs.contains("-auto-approve")) {
                additionalArgs
            } else {
                additionalArgs + "-auto-approve"
            }
        println("${AnsiColors.BLUE}Calling terraformService.apply with args: $applyArgsWithAutoApprove${AnsiColors.RESET}")
        val result = terraformService.apply(additionalArgs = applyArgsWithAutoApprove)
        println("${AnsiColors.BLUE}terraformService.apply returned: $result${AnsiColors.RESET}")
        return if (result.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (result.message()?.contains("Terraform configuration not found") == true) {
                0
            } else {
                println("${AnsiColors.RED}Terraform apply failed: ${result.message()} (exit code: ${result.exitCode()})${AnsiColors.RESET}")
                result.exitCode()
            }
        } else {
            0
        }
    }
}
