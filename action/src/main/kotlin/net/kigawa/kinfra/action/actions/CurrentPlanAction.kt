package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class CurrentPlanAction : Action {
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

        // プロジェクト名を表示
        println("${AnsiColors.BLUE}Planning Terraform changes for current directory:${AnsiColors.RESET} ${currentDir.absolutePath}")

        // plan実行前に自動でinitを実行
        println("${AnsiColors.BLUE}Initializing Terraform...${AnsiColors.RESET}")
        val initProcess = ProcessBuilder("terraform", "init", "-input=false")
            .directory(currentDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val initExitCode = initProcess.waitFor()
        if (initExitCode != 0) {
            println("${AnsiColors.RED}Terraform init failed in current directory${AnsiColors.RESET}")
            return initExitCode
        }

        // backend.tfvarsファイルが存在するかチェック
        val backendTfvarsFile = File(currentDir, "backend.tfvars")
        val planArgs = if (backendTfvarsFile.exists()) {
            listOf("terraform", "plan", "-input=false", "-backend-config=backend.tfvars") + args
        } else {
            listOf("terraform", "plan", "-input=false") + args
        }

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