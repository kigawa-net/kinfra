package net.kigawa.kinfra.action.actions
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message

class ValidateAction(
    private val terraformService: TerraformService,
    private val gitHelper: GitHelper,
) : Action {
    override fun execute(args: List<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        // Terraform設定が取得できない場合は静かにスキップ
        val config = terraformService.terraformConfig

        val result = terraformService.validate(quiet = false)

        // エラーが発生した場合、プロジェクト情報を表示
        if (result.isFailure()) {
            config?.let { println("${AnsiColors.RED}Error in project:${AnsiColors.RESET} ${it.workingDirectory.absolutePath}") }
            result.message()?.let { println("${AnsiColors.RED}Details: $it${AnsiColors.RESET}") }
        }

        return result.exitCode()
    }

    override fun getDescription(): String {
        return "Validate the configuration files"
    }
}
