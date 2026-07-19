package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.model.conf.BackendConfigResolver
import net.kigawa.kinfra.model.execution.SubProjectChangeFilter
import net.kigawa.kinfra.model.execution.SubProjectChangeFilterFactory
import net.kigawa.kinfra.model.execution.SubProjectExecutor
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.sub.SubProject
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message
import kotlinx.coroutines.runBlocking
import java.io.File

class ApplyAction(
    private val terraformService: TerraformService,
    private val subProjectExecutor: SubProjectExecutor? = null,
    private val bitwardenSecretManagerRepository: BitwardenSecretManagerRepository? = null,
    private val changeFilterFactory: SubProjectChangeFilterFactory? = null,
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

        if (result.isFailure()) {
            return result.exitCode()
        }

        // サブプロジェクトでもapplyを実行（前回apply成功時から変更のあったものだけ）
        val executor = subProjectExecutor ?: return result.exitCode()
        val allSubProjects = executor.getSubProjects()
        if (allSubProjects.isEmpty()) {
            return result.exitCode()
        }

        val changeFilter = createChangeFilter(executor)
        val subProjectsWithDirs = allSubProjects.map { it to executor.resolveSubProjectDir(it) }
        val changedSubProjects = runBlocking { changeFilter.filterChanged(subProjectsWithDirs) }
        val skippedCount = allSubProjects.size - changedSubProjects.size

        println()
        println(
            "${AnsiColors.BLUE}Found ${allSubProjects.size} sub-project(s), " +
                "${changedSubProjects.size} changed (${skippedCount} skipped, no changes detected)${AnsiColors.RESET}",
        )

        if (changedSubProjects.isEmpty()) {
            return result.exitCode()
        }

        val subResult =
            executor.executeInSubProjects(changedSubProjects.map { it.first }) { subProject, subProjectDir ->
                applySubProject(executor, subProject, subProjectDir, changeFilter)
            }

        if (subResult != 0) {
            println("${AnsiColors.RED}Sub-project apply failed${AnsiColors.RESET}")
            return subResult
        }

        return result.exitCode()
    }

    private fun applySubProject(
        executor: SubProjectExecutor,
        subProject: SubProject,
        subProjectDir: File,
        changeFilter: SubProjectChangeFilter,
    ): Int {
        println(
            "${AnsiColors.BLUE}Applying Terraform changes for sub-project:${AnsiColors.RESET} " +
                "${subProject.name} (${subProjectDir.absolutePath})",
        )

        // サブプロジェクトのマージされたbackendConfigを読み込み、bws()マーカーを解決
        val backendConfig =
            BackendConfigResolver.flattenAndResolve(
                executor.getMergedBackendConfig(subProject),
                bitwardenSecretManagerRepository,
            )

        println("${AnsiColors.BLUE}Initializing Terraform for sub-project...${AnsiColors.RESET}")
        val initArgs = mutableListOf("terraform", "init", "-input=false")
        backendConfig.forEach { (key, value) ->
            initArgs.add("-backend-config=$key=$value")
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
            return initExitCode
        }

        // -backend-configはterraform init専用のフラグでapplyでは受け付けられない
        // (backendは直前のinitで既に設定済み)
        val applyArgs = mutableListOf("terraform", "apply", "-input=false", "-auto-approve")

        val process =
            ProcessBuilder(applyArgs)
                .directory(subProjectDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            runBlocking { changeFilter.recordSuccess(subProject, subProjectDir) }
        }
        return exitCode
    }

    /**
     * 変更検出フィルタを、親プロジェクトのbackendConfig（bws()解決済み）から組み立てる。
     * R2の認証情報が揃っていない場合はfail-open（全サブプロジェクトを対象にする）。
     */
    private fun createChangeFilter(executor: SubProjectExecutor): SubProjectChangeFilter {
        val factory = changeFilterFactory ?: return SubProjectChangeFilter.NOOP
        val resolvedBackendConfig =
            BackendConfigResolver.flattenAndResolve(
                executor.getBackendConfig(),
                bitwardenSecretManagerRepository,
            )
        val (r2Bucket, r2Endpoint) = executor.getR2Config()
        val resolvedR2Bucket = BackendConfigResolver.resolveValue(r2Bucket, bitwardenSecretManagerRepository)
        val resolvedR2Endpoint = BackendConfigResolver.resolveValue(r2Endpoint, bitwardenSecretManagerRepository)
        val parentProjectName = executor.getParentProjectName() ?: return SubProjectChangeFilter.NOOP
        return factory.create(resolvedBackendConfig, resolvedR2Bucket, resolvedR2Endpoint, parentProjectName)
    }

    override fun getDescription(): String {
        return "Apply the changes required to reach the desired state"
    }
}
