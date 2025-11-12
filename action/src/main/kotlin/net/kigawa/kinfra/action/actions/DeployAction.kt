package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.config.ConfigRepository
import net.kigawa.kinfra.model.execution.ActionExecutor
import net.kigawa.kinfra.model.execution.DeploymentPipeline
import net.kigawa.kinfra.model.execution.ExecutionStep
import net.kigawa.kinfra.model.execution.SubProjectExecutor
import net.kigawa.kinfra.model.logging.Logger
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.util.AnsiColors

class DeployAction(
    private val terraformService: TerraformService,
    configRepository: ConfigRepository,
    loginRepo: LoginRepo,
    private val logger: Logger,
) : Action {
    private val executor = ActionExecutor(logger)
    private val pipeline = DeploymentPipeline(terraformService)
    private val subProjectExecutor = SubProjectExecutor(configRepository, loginRepo)

    override fun execute(args: List<String>): Int {
        val additionalArgs = args.filter { it != "--auto-selected" }

        logger.info("Starting full deployment pipeline")
        logger.info("Current working directory: ${System.getProperty("user.dir")}")

        // Execute parent project first
        logger.info("Executing parent project")

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
            logger.error("Parent project deployment failed with exit code: $result")
            logger.warn("Check the logs above for detailed error information")
            return result
        }

        logger.info("Parent project completed successfully")

        // Execute sub-projects
        val subProjects = subProjectExecutor.getSubProjects()
        if (subProjects.isNotEmpty()) {
            logger.info("Found ${subProjects.size} sub-project(s)")

            val subResult =
                subProjectExecutor.executeInSubProjects(subProjects) { _, _ ->
                    executeSubProjectDeployment(additionalArgs)
                }

            if (subResult != 0) {
                logger.error("Sub-project deployment failed")
                return subResult
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
        val subExecutor = ActionExecutor(logger)

        val steps =
            listOf(
                ExecutionStep("Initialize Terraform") { subPipeline.initializeTerraform(additionalArgs) },
                ExecutionStep("Create execution plan") { subPipeline.createExecutionPlan(additionalArgs) },
                ExecutionStep("Apply changes") { subPipeline.applyChanges(additionalArgs) },
            )

        return subExecutor.executeSteps(steps)
    }

    private fun handleSuccessfulDeployment() {
        logger.info("Deployment completed successfully!")
    }

    override fun getDescription(): String {
        return "Full deployment pipeline (init → plan → apply)"
    }
}
