package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File
import java.nio.file.Paths

class CurrentPlanAction(private val configRepository: ConfigRepository) : Action {
    override fun execute(args: List<String>): Int {

        val currentDir = File(".").absoluteFile

        // カレントディレクトリにTerraformファイルがあるかチェック
        val terraformFiles = listOf("main.tf", "variables.tf", "outputs.tf", "terraform.tfvars")
        val hasTerraformFiles = terraformFiles.any { File(currentDir, it).exists() }

        if (!hasTerraformFiles) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} No Terraform files found in current directory (${currentDir.absolutePath})")
            println("Expected files: ${terraformFiles.joinToString(", ")}")
            return 1
        }

        // kinfra.yamlからbackendConfigを読み込み
        val kinfraConfigPath = Paths.get(currentDir.absolutePath, "kinfra.yaml")
        val kinfraParentConfigPath = Paths.get(currentDir.absolutePath, "kinfra-parent.yaml")
        println("Config paths: $kinfraConfigPath, $kinfraParentConfigPath")

        val backendConfig = if (configRepository.kinfraConfigExists(kinfraConfigPath.toString())) {
            println("kinfra.yaml exists")
            val config = configRepository.loadKinfraConfig(kinfraConfigPath)
            val bc = config?.rootProject?.terraform?.backendConfig
            println("Backend config from kinfra.yaml: $bc")
            bc
        } else if (configRepository.kinfraParentConfigExists(kinfraParentConfigPath.toString())) {
            println("kinfra-parent.yaml exists")
            val config = configRepository.loadKinfraParentConfig(kinfraParentConfigPath.toString())
            val bc = config?.terraform?.backendConfig
            println("Backend config from kinfra-parent.yaml: $bc")
            bc
        } else {
            println("No config file found")
            emptyMap()
        }

        // backend.tfvarsファイルが存在するかチェック
        val backendTfvarsFile = File(currentDir, "backend.tfvars")

        // プロジェクト名を表示
        println("${AnsiColors.BLUE}Planning Terraform changes for current directory:${AnsiColors.RESET} ${currentDir.absolutePath}")

        // plan実行前に自動でinitを実行
        println("${AnsiColors.BLUE}Initializing Terraform...${AnsiColors.RESET}")
        val initArgs = mutableListOf("terraform", "init", "-input=false")

        // backendConfigから-backend-configオプションを追加
        backendConfig?.forEach { (key, value) ->
            initArgs.add("-backend-config=$key=$value")
        }

        // backend.tfvarsが存在する場合も追加
        if (backendTfvarsFile.exists()) {
            initArgs.add("-backend-config=backend.tfvars")
        }

        println("Init args: ${initArgs.joinToString(" ")}")

        val initProcess = ProcessBuilder(initArgs)
            .directory(currentDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val initExitCode = initProcess.waitFor()
        if (initExitCode != 0) {
            println("${AnsiColors.RED}Terraform init failed in current directory${AnsiColors.RESET}")
            return initExitCode
        }

        val planArgs = mutableListOf("terraform", "plan", "-input=false")

        // backendConfigから-backend-configオプションを追加
        backendConfig?.forEach { (key, value) ->
            planArgs.add("-backend-config=$key=$value")
        }

        // backend.tfvarsが存在する場合も追加
        if (backendTfvarsFile.exists()) {
            planArgs.add("-backend-config=backend.tfvars")
        }

        planArgs.addAll(args)

        val process = ProcessBuilder(planArgs)
            .directory(currentDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val exitCode = process.waitFor()

        // エラーが発生した場合、ディレクトリ情報を表示
        if (exitCode != 0) {
            println("${AnsiColors.RED}Error in current directory:${AnsiColors.RESET} ${currentDir.absolutePath}")
        }

        return exitCode
    }

    override fun getDescription(): String {
        return "Create an execution plan for the current directory"
    }
}