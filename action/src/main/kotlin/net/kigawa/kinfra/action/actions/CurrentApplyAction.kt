package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.model.conf.BackendConfigResolver
import net.kigawa.kinfra.model.config.ConfigRepository
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File
import java.nio.file.Paths

/**
 * カレントディレクトリでterraform applyを実行する。`kinfra login`を経由せず、
 * 既にcheckout済みのディレクトリ（CIのmatrixジョブ等）に対してそのまま使える。
 * 設定解決ロジックはCurrentPlanActionと同一。
 */
class CurrentApplyAction(
    private val configRepository: ConfigRepository,
    private val bitwardenSecretManagerRepository: BitwardenSecretManagerRepository? = null,
) : Action {
    private fun flattenBackendConfig(backendConfig: Map<String, Any>): Map<String, String> =
        BackendConfigResolver.flattenAndResolve(backendConfig, bitwardenSecretManagerRepository)

    override fun execute(args: List<String>): Int {
        val currentDir = File(".").absoluteFile

        val terraformFiles = listOf("main.tf", "variables.tf", "outputs.tf", "terraform.tfvars")
        val hasTerraformFiles = terraformFiles.any { File(currentDir, it).exists() }

        if (!hasTerraformFiles) {
            println(
                "${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} No Terraform files found in " +
                    "current directory (${currentDir.absolutePath})",
            )
            println("Expected files: ${terraformFiles.joinToString(", ")}")
            return 1
        }

        // kinfra.ktsとkinfra-parent.ktsからbackendConfigを読み込み、マージする
        val kinfraConfigPath = Paths.get(currentDir.absolutePath, "kinfra.kts")
        val kinfraParentConfigPath = Paths.get(currentDir.absolutePath, "kinfra-parent.kts")

        val parentBackendConfig: Map<String, Any> =
            if (configRepository.kinfraParentConfigExists(kinfraParentConfigPath.toString())) {
                val config = configRepository.loadKinfraParentConfig(kinfraParentConfigPath.toString())
                config?.terraform?.backendConfig ?: emptyMap()
            } else {
                emptyMap()
            }

        val subProjectBackendConfig: Map<String, Any> =
            if (configRepository.kinfraConfigExists(kinfraConfigPath.toString())) {
                val config = configRepository.loadKinfraConfig(kinfraConfigPath)
                config?.rootProject?.terraform?.backendConfig ?: emptyMap()
            } else {
                emptyMap()
            }

        // 親プロジェクトとサブプロジェクトの設定をマージ（サブプロジェクトが優先）
        val backendConfig = parentBackendConfig + subProjectBackendConfig

        val backendTfvarsFile = File(currentDir, "backend.tfvars")

        println("${AnsiColors.BLUE}Applying Terraform changes for current directory:${AnsiColors.RESET} ${currentDir.absolutePath}")

        println("${AnsiColors.BLUE}Initializing Terraform...${AnsiColors.RESET}")
        val initArgs = mutableListOf("terraform", "init", "-input=false")
        val flattenedConfig = flattenBackendConfig(backendConfig)
        flattenedConfig.forEach { (key, value) ->
            initArgs.add("-backend-config=$key=$value")
        }
        if (backendTfvarsFile.exists()) {
            initArgs.add("-backend-config=backend.tfvars")
        }

        val initProcess =
            ProcessBuilder(initArgs)
                .directory(currentDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        val initExitCode = initProcess.waitFor()
        if (initExitCode != 0) {
            println("${AnsiColors.RED}Terraform init failed in current directory${AnsiColors.RESET}")
            return initExitCode
        }

        // ドキュメント通り自動承認する（未指定の場合のみ付与）
        val argsWithAutoApprove =
            if (args.contains("-auto-approve")) {
                args
            } else {
                args + "-auto-approve"
            }

        // -backend-config はterraform initのみが受け付けるオプションであり、
        // applyには渡さない(渡すとapplyがエラーになる)。
        val applyArgs = mutableListOf("terraform", "apply", "-input=false")
        applyArgs.addAll(argsWithAutoApprove)

        val process =
            ProcessBuilder(applyArgs)
                .directory(currentDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        val exitCode = process.waitFor()

        if (exitCode != 0) {
            println("${AnsiColors.RED}Error in current directory:${AnsiColors.RESET} ${currentDir.absolutePath}")
        }

        return exitCode
    }

    override fun getDescription(): String {
        return "Apply the changes required to reach the desired state for the current directory"
    }
}
