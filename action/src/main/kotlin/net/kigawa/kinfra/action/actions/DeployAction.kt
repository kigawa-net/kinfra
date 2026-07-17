package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.model.conf.BackendConfigResolver
import net.kigawa.kinfra.model.config.ConfigRepository
import net.kigawa.kinfra.model.execution.ActionExecutor
import net.kigawa.kinfra.model.execution.DeploymentPipeline
import net.kigawa.kinfra.model.execution.ExecutionStep
import net.kigawa.kinfra.model.execution.SubProjectChangeFilter
import net.kigawa.kinfra.model.execution.SubProjectChangeFilterFactory
import net.kigawa.kinfra.model.execution.SubProjectExecutor
import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kodel.api.log.traceignore.error
import net.kigawa.kodel.api.log.traceignore.warn
import kotlinx.coroutines.runBlocking

class DeployAction(
    private val terraformService: TerraformService,
    configRepository: ConfigRepository,
    loginRepo: LoginRepo,
    private val kogger: Kogger,
    private val bitwardenSecretManagerRepository: BitwardenSecretManagerRepository? = null,
    private val changeFilterFactory: SubProjectChangeFilterFactory? = null,
) : Action {
    private val executor = ActionExecutor(kogger)
    private val pipeline = DeploymentPipeline(terraformService)
    private val subProjectExecutor = SubProjectExecutor(configRepository, loginRepo)

    override fun execute(args: List<String>): Int {
        val additionalArgs = args.filter { it != "--auto-selected" }

        kogger.info("Starting full deployment pipeline")
        kogger.info("Current working directory: ${System.getProperty("user.dir")}")

        // Execute parent project first
        kogger.info("Executing parent project")

        val steps =
            listOf(
                ExecutionStep("Initialize Terraform") {
                    executor.executeWithErrorHandling("Initialize Terraform", { pipeline.initializeTerraform(additionalArgs) })
                },
                ExecutionStep("Create execution plan") {
                    executor.executeWithErrorHandling("Create execution plan", { pipeline.createExecutionPlan(additionalArgs) })
                },
                ExecutionStep(
                    "Apply changes",
                ) { executor.executeWithErrorHandling("Apply changes", { pipeline.applyChanges(additionalArgs) }) },
            )

        val result = executor.executeSteps(steps)

        if (result != 0) {
            kogger.error("Parent project deployment failed with exit code: $result")
            kogger.warn("Check the logs above for detailed error information")
            return result
        }

        kogger.info("Parent project completed successfully")

        // Execute sub-projects (前回apply成功時から変更のあったものだけ)
        val allSubProjects = subProjectExecutor.getSubProjects()
        if (allSubProjects.isNotEmpty()) {
            val changeFilter = createChangeFilter()
            val subProjectsWithDirs = allSubProjects.map { it to subProjectExecutor.resolveSubProjectDir(it) }
            val changedSubProjects = runBlocking { changeFilter.filterChanged(subProjectsWithDirs) }
            val skippedCount = allSubProjects.size - changedSubProjects.size

            kogger.info(
                "Found ${allSubProjects.size} sub-project(s), " +
                    "${changedSubProjects.size} changed ($skippedCount skipped, no changes detected)",
            )

            if (changedSubProjects.isNotEmpty()) {
                val subResult =
                    subProjectExecutor.executeInSubProjects(changedSubProjects.map { it.first }) { subProject, subProjectDir ->
                        val stepResult = executeSubProjectDeployment(additionalArgs)
                        if (stepResult == 0) {
                            runBlocking { changeFilter.recordSuccess(subProject, subProjectDir) }
                        }
                        stepResult
                    }

                if (subResult != 0) {
                    kogger.error("Sub-project deployment failed")
                    return subResult
                }
            }
        }

        // Handle post-deployment actions
        handleSuccessfulDeployment()

        return 0
    }

    private fun executeSubProjectDeployment(additionalArgs: List<String>): Int {
        // Create new instances for sub-project execution
        // Note: TerraformService will use the current working directory
        val subPipeline = DeploymentPipeline(terraformService)
        val subExecutor = ActionExecutor(kogger)

        val steps =
            listOf(
                ExecutionStep("Initialize Terraform") { subPipeline.initializeTerraform(additionalArgs) },
                ExecutionStep("Create execution plan") { subPipeline.createExecutionPlan(additionalArgs) },
                ExecutionStep("Apply changes") { subPipeline.applyChanges(additionalArgs) },
            )

        return subExecutor.executeSteps(steps)
    }

    private fun handleSuccessfulDeployment() {
        kogger.info("Deployment completed successfully!")
    }

    /**
     * 変更検出フィルタを、親プロジェクトのbackendConfig（bws()解決済み）から組み立てる。
     * R2の認証情報が揃っていない場合はfail-open（全サブプロジェクトを対象にする）。
     */
    private fun createChangeFilter(): SubProjectChangeFilter {
        val factory = changeFilterFactory ?: return SubProjectChangeFilter.NOOP
        val resolvedBackendConfig =
            BackendConfigResolver.flattenAndResolve(
                subProjectExecutor.getBackendConfig(),
                bitwardenSecretManagerRepository,
            )
        val parentProjectName = subProjectExecutor.getParentProjectName() ?: return SubProjectChangeFilter.NOOP
        return factory.create(resolvedBackendConfig, parentProjectName)
    }

    override fun getDescription(): String {
        return "Full deployment pipeline (init → plan → apply)"
    }
}
