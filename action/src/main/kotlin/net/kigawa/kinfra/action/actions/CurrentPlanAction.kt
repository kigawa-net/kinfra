package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

class CurrentPlanAction : Action {
    override fun execute(args: List<String>): Int {
        val currentDir = File(".").absoluteFile

        // カレントディレクトリにTerraformファイルがあるかチェック
        val requiredFiles = listOf("main.tf")
        val optionalFiles = listOf("variables.tf", "outputs.tf", "terraform.tfvars")
        val hasRequiredFiles = requiredFiles.all { File(currentDir, it).exists() }
        val hasOptionalFiles = optionalFiles.any { File(currentDir, it).exists() }

        if (!hasRequiredFiles) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} No Terraform configuration found in current directory (${currentDir.absolutePath})")
            println("Required files: ${requiredFiles.joinToString(", ")}")
            if (hasOptionalFiles) {
                println("Optional files found: ${optionalFiles.filter { File(currentDir, it).exists() }.joinToString(", ")}")
            }
            return 1
        }

        if (!hasRequiredFiles) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} No Terraform configuration found in current directory (${currentDir.absolutePath})")
            println("Required files: ${requiredFiles.joinToString(", ")}")
            if (hasOptionalFiles) {
                println("Optional files found: ${optionalFiles.filter { File(currentDir, it).exists() }.joinToString(", ")}")
            }
            return 1
        }

        // plan実行前に自動でinitを実行
        println("${AnsiColors.BLUE}Initializing Terraform in current directory...${AnsiColors.RESET}")
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

        println("${AnsiColors.BLUE}Planning Terraform changes in current directory:${AnsiColors.RESET} ${currentDir.absolutePath}")

        val process = ProcessBuilder(planArgs)
            .directory(currentDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        return process.waitFor()
    }

    override fun getDescription(): String {
        return "Create an execution plan for the current directory"
    }
}