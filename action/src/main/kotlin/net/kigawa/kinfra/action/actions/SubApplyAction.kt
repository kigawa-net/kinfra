package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.model.conf.BackendConfigResolver
import net.kigawa.kinfra.model.execution.SubProjectExecutor
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

/**
 * 指定したサブプロジェクトでterraform applyを実行する。
 * 設定解決ロジックはSubPlanActionと同一（親+サブプロジェクトのbackendConfigをマージし、
 * bws()マーカーをBitwarden Secret Managerから解決する）。
 */
class SubApplyAction(
    private val loginRepo: LoginRepo,
    private val subProjectExecutor: SubProjectExecutor,
    private val bitwardenSecretManagerRepository: BitwardenSecretManagerRepository? = null,
) : Action {
    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            showUsage()
            return 1
        }

        val subProjectName = args[0]
        val parentConfig = loginRepo.loadKinfraBaseConfig()

        if (parentConfig == null) {
            println(
                "${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration file not found: ${loginRepo.kinfraBaseConfigPath()}",
            )
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra sub add <project-name>' to create a configuration file",
            )
            return 1
        }

        val subProject = parentConfig.subProjects.find { it.name == subProjectName }
        if (subProject == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project '$subProjectName' not found")
            println("${AnsiColors.BLUE}Available sub-projects:${AnsiColors.RESET}")
            if (parentConfig.subProjects.isEmpty()) {
                println("  ${AnsiColors.YELLOW}(none)${AnsiColors.RESET}")
            } else {
                parentConfig.subProjects.forEach { project ->
                    val displayText =
                        if (project.path == project.name) {
                            project.name
                        } else {
                            "${project.name}:${project.path}"
                        }
                    println("  - $displayText")
                }
            }
            return 1
        }

        val additionalArgs = args.drop(1)

        // サブプロジェクトでapplyを実行
        val result =
            subProjectExecutor.executeInSubProjects(listOf(subProject)) { _, subProjectDir ->
                val backendTfvarsFile = File(subProjectDir, "backend.tfvars")
                val mergedBackendConfig = subProjectExecutor.getMergedBackendConfig(subProject)
                val flattenedBackendConfig =
                    BackendConfigResolver.flattenAndResolve(mergedBackendConfig, bitwardenSecretManagerRepository)

                // apply前にinitを実行
                println("${AnsiColors.BLUE}Initializing Terraform for sub-project ${subProject.name}...${AnsiColors.RESET}")
                val initArgs = mutableListOf("terraform", "init", "-input=false")
                flattenedBackendConfig.forEach { (key, value) ->
                    initArgs.add("-backend-config=$key=$value")
                }
                if (backendTfvarsFile.exists()) {
                    initArgs.add("-backend-config=backend.tfvars")
                }
                val initProcess =
                    ProcessBuilder(initArgs)
                        .directory(subProjectDir)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start()

                val initExitCode = initProcess.waitFor()
                if (initExitCode != 0) {
                    println("${AnsiColors.RED}Terraform init failed for sub-project ${subProject.name}${AnsiColors.RESET}")
                    return@executeInSubProjects initExitCode
                }

                // ドキュメント通り自動承認する（未指定の場合のみ付与）
                val argsWithAutoApprove =
                    if (additionalArgs.contains("-auto-approve")) {
                        additionalArgs
                    } else {
                        additionalArgs + "-auto-approve"
                    }

                val applyArgs = mutableListOf("terraform", "apply", "-input=false")
                flattenedBackendConfig.forEach { (key, value) ->
                    applyArgs.add("-backend-config=$key=$value")
                }
                if (backendTfvarsFile.exists()) {
                    applyArgs.add("-backend-config=backend.tfvars")
                }
                applyArgs.addAll(argsWithAutoApprove)

                // サブプロジェクトディレクトリでterraform applyを実行
                val process =
                    ProcessBuilder(applyArgs)
                        .directory(subProjectDir)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start()

                process.waitFor()
            }

        return result
    }

    override fun getDescription(): String {
        return "Run terraform apply in a sub-project"
    }

    override fun showHelp() {
        println("Usage: kinfra sub apply <sub-project-name>")
        println()
        println("Run terraform apply in the specified sub-project.")
        println()
        println("Arguments:")
        println("  <sub-project-name>  Name of the sub-project")
        println()
        println("Examples:")
        println("  kinfra sub apply my-project")
    }

    private fun showUsage() {
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name is required")
        println()
        showHelp()
    }
}
