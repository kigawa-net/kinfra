package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.config.ConfigRepository
import net.kigawa.kinfra.model.execution.ActionExecutor
import net.kigawa.kinfra.model.execution.DeploymentPipeline
import net.kigawa.kinfra.model.execution.ExecutionStep
import net.kigawa.kinfra.model.execution.SubProjectExecutor
import net.kigawa.kinfra.model.logging.Logger
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.bitwarden.BitwardenRepository
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors

class DeployAction(
    private val terraformService: TerraformService,
    private val bitwardenRepository: BitwardenRepository,
    configRepository: ConfigRepository,
    loginRepo: LoginRepo,
    private val logger: Logger
) : Action {

    private val executor = ActionExecutor(logger)
    private val pipeline = DeploymentPipeline(terraformService, bitwardenRepository)
    private val subProjectExecutor = SubProjectExecutor(configRepository, loginRepo)

    override fun execute(args: List<String>): Int {
        val additionalArgs = args.filter { it != "--auto-selected" }

        println("${AnsiColors.BLUE}Starting full deployment pipeline${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Current working directory: ${System.getProperty("user.dir")}${AnsiColors.RESET}")
        println()

        // Execute parent project first
        println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Executing parent project${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
        println()

        val steps = listOf(
            ExecutionStep("Initialize Terraform") { executor.executeWithErrorHandling("Initialize Terraform", { pipeline.initializeTerraform(additionalArgs) }) },
            ExecutionStep("Create execution plan") { executor.executeWithErrorHandling("Create execution plan", { pipeline.createExecutionPlan(additionalArgs) }) },
            ExecutionStep("Apply changes") { executor.executeWithErrorHandling("Apply changes", { pipeline.applyChanges(additionalArgs) }) }
        )

        val result = executor.executeSteps(steps)

        if (result != 0) {
            println("${AnsiColors.RED}Parent project deployment failed with exit code: $result${AnsiColors.RESET}")
            println("${AnsiColors.YELLOW}Check the logs above for detailed error information${AnsiColors.RESET}")
            return result
        }

        println()
        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Parent project completed successfully")

        // Execute sub-projects
        val subProjects = subProjectExecutor.getSubProjects()
        if (subProjects.isNotEmpty()) {
            println()
            println("${AnsiColors.BLUE}Found ${subProjects.size} sub-project(s)${AnsiColors.RESET}")

            val subResult = subProjectExecutor.executeInSubProjects(subProjects) { _, _ ->
                executeSubProjectDeployment(additionalArgs)
            }

            if (subResult != 0) {
                println("${AnsiColors.RED}Sub-project deployment failed${AnsiColors.RESET}")
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
        val subPipeline = DeploymentPipeline(terraformService, bitwardenRepository)
        val subExecutor = ActionExecutor(logger)

        val steps = listOf(
            ExecutionStep("Initialize Terraform") { subPipeline.initializeTerraform(additionalArgs) },
            ExecutionStep("Create execution plan") { subPipeline.createExecutionPlan(additionalArgs) },
            ExecutionStep("Apply changes") { subPipeline.applyChanges(additionalArgs) }
        )

        return subExecutor.executeSteps(steps)
    }
    
    private fun handleSuccessfulDeployment() {
        println()
        println("${AnsiColors.GREEN}✅ Deployment completed successfully!${AnsiColors.RESET}")

        // Auto git push after successful deployment
        println()
        println("${AnsiColors.BLUE}Pushing to remote repository...${AnsiColors.RESET}")
        val pushResult = pipeline.pushToGit()
        if (pushResult != 0) {
            println("${AnsiColors.YELLOW}⚠${AnsiColors.RESET} Failed to push to remote repository (non-fatal)")
        }
    }

override fun getDescription(): String {
        return "Full deployment pipeline (init → plan → apply)"
    }


}
