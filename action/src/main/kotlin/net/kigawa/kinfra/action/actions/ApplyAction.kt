package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message

class ApplyAction(
    private val terraformService: TerraformService,
) : Action {
    override fun execute(args: List<String>): Int {
        // Terraform設定が取得できない場合は静かにスキップ
        val config = terraformService.terraformConfig
        if (config == null) {
            return 0
        }

        // Check if first arg is a plan file
        val planFile =
            if (args.isNotEmpty() &&
                (args[0].endsWith(".tfplan") || args[0] == "tfplan")
            ) {
                args[0]
            } else {
                null
            }

        val argsWithoutPlan = if (planFile != null) args.drop(1) else args

        // ドキュメント通り自動承認する（未指定の場合のみ付与）
        val argsWithAutoApprove =
            if (argsWithoutPlan.contains("-auto-approve")) {
                argsWithoutPlan
            } else {
                argsWithoutPlan + "-auto-approve"
            }

        val result = terraformService.apply(planFile, argsWithAutoApprove, quiet = false)

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
